package com.reelblocker.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.reelblocker.app.data.UserPreferencesRepository
import com.reelblocker.app.data.ViewIdProvider
import com.reelblocker.app.model.UserPreferences
import com.reelblocker.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Core Accessibility Service that detects and blocks short-form video content
 * (Instagram Reels & Explore, and YouTube Shorts) by inspecting the UI node hierarchy.
 *
 * Designed with precision:
 * 1. NEVER blocks Instagram Direct Messages / Chats (reading, typing, voice notes, media).
 * 2. NEVER blocks Instagram Profile page or regular Home feed posts.
 * 3. Immediately redirects to the Home tab when the user taps the dedicated "Reels" bottom tab.
 * 4. Optionally redirects to the Home tab when the user taps the "Explore" bottom tab.
 * 5. Exits full-screen Reels viewer while keeping DM reel viewing functional.
 * 6. Smooth YouTube Shorts handling without false-positive blocks on the Home feed carousel.
 */
class BlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var viewIdProvider: ViewIdProvider

    private var currentPreferences = UserPreferences()
    private var lastBlockTimeMs = 0L
    private var lastAppSessionPackage: String? = null
    private var lastAppSessionTimeMs = 0L

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = UserPreferencesRepository(applicationContext)
        viewIdProvider = ViewIdProvider()

        // Observe preference updates reactively
        serviceScope.launch {
            preferencesRepository.preferencesFlow.collectLatest { prefs ->
                currentPreferences = prefs
            }
        }

        Log.d(Constants.LOG_TAG, "BlockerAccessibilityService created")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Check remote kill switch first
        if (viewIdProvider.isKillSwitchActive()) return

        // Protection is active if either Master is ON or Scheduled Focus Hours are active (and not snoozed)
        if (!currentPreferences.isProtectionActive) return

        val packageName = event.packageName?.toString() ?: return

        // Track Clean Sessions: detect when user opens Instagram or YouTube
        val currentTime = System.currentTimeMillis()
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            (packageName == Constants.PACKAGE_INSTAGRAM || packageName == Constants.PACKAGE_YOUTUBE)) {
            if (lastAppSessionPackage != packageName || (currentTime - lastAppSessionTimeMs) > 45_000L) {
                lastAppSessionPackage = packageName
                lastAppSessionTimeMs = currentTime
                serviceScope.launch {
                    preferencesRepository.recordSessionStart()
                }
            }
        }

        val rootNode = rootInActiveWindow ?: return

        try {
            when (packageName) {
                Constants.PACKAGE_INSTAGRAM -> {
                    handleInstagramEvent(rootNode)
                }
                Constants.PACKAGE_YOUTUBE -> {
                    if (currentPreferences.blockYouTubeShorts) {
                        handleYouTubeEvent(rootNode)
                    }
                }
            }
        } finally {
            safeRecycle(rootNode)
        }
    }

    // =========================================================================
    // INSTAGRAM LOGIC
    // =========================================================================

    private fun handleInstagramEvent(rootNode: AccessibilityNodeInfo) {
        // 1. SAFETY FIRST: If user is anywhere inside Direct Messages/Chat, NEVER block!
        if (isInsideInstagramDirectMessage(rootNode)) {
            return
        }

        // 2. SAFETY: If user is on the Profile page, NEVER block!
        if (isInstagramProfileTabSelected(rootNode)) {
            return
        }

        // 3. TARGET: Check if the dedicated "Reels" bottom navigation tab is selected
        if (currentPreferences.blockInstagramReels && isInstagramReelsTabSelected(rootNode)) {
            val redirected = redirectToInstagramHomeTab(rootNode)
            if (redirected) {
                incrementBlockedCountAsync()
                Log.d(Constants.LOG_TAG, "Redirected from Instagram Reels tab to Home feed")
                return
            }
        }

        // 4. TARGET: Check if the "Explore" (Search & Explore) bottom tab is selected
        if (currentPreferences.blockInstagramExplore && isInstagramExploreTabSelected(rootNode)) {
            val redirected = redirectToInstagramHomeTab(rootNode)
            if (redirected) {
                incrementBlockedCountAsync()
                Log.d(Constants.LOG_TAG, "Redirected from Instagram Explore tab to Home feed")
                return
            }
        }

        // 5. TARGET: Check if full-screen Reels player is active outside of DMs
        if (currentPreferences.blockInstagramReels && isInstagramClipsViewerActive(rootNode)) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBlockTimeMs >= 500L) {
                lastBlockTimeMs = currentTime
                // Try redirecting to Home first; if not in tab bar, press Back
                if (!redirectToInstagramHomeTab(rootNode)) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
                incrementBlockedCountAsync()
                Log.d(Constants.LOG_TAG, "Exited Instagram full-screen Reels viewer")
            }
        }
    }

    /**
     * Detects if the current screen is inside a Direct Message conversation or DM inbox.
     * When true, NO blocking actions will ever take place.
     */
    private fun isInsideInstagramDirectMessage(rootNode: AccessibilityNodeInfo): Boolean {
        val dmViewIds = listOf(
            "com.instagram.android:id/row_thread_composer_edittext",
            "com.instagram.android:id/direct_message_list",
            "com.instagram.android:id/direct_thread_layout",
            "com.instagram.android:id/thread_title",
            "com.instagram.android:id/direct_text_message_text_view",
            "com.instagram.android:id/voice_message_recording_view",
            "com.instagram.android:id/direct_media_preview_container"
        )

        for (viewId in dmViewIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                nodes.forEach { safeRecycle(it) }
                return true
            }
        }
        return false
    }

    /**
     * Checks if the dedicated "Reels" bottom tab is currently selected.
     */
    private fun isInstagramReelsTabSelected(rootNode: AccessibilityNodeInfo): Boolean {
        // Direct View ID check on modern Instagram builds
        val clipsTabNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_tab")
        if (!clipsTabNodes.isNullOrEmpty()) {
            for (node in clipsTabNodes) {
                val selected = node.isSelected
                safeRecycle(node)
                if (selected) return true
            }
        }

        // Content description check strictly within bottom navigation area
        return isBottomNavigationTabSelected(rootNode, listOf("Reels", "clips_tab"))
    }

    /**
     * Checks if the "Search and explore" bottom tab is currently selected.
     */
    private fun isInstagramExploreTabSelected(rootNode: AccessibilityNodeInfo): Boolean {
        val searchTabNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/search_tab")
        if (!searchTabNodes.isNullOrEmpty()) {
            for (node in searchTabNodes) {
                val selected = node.isSelected
                safeRecycle(node)
                if (selected) return true
            }
        }

        return isBottomNavigationTabSelected(rootNode, listOf("Search and explore", "Search", "Explore"))
    }

    /**
     * Checks if the "Profile" bottom tab is selected.
     */
    private fun isInstagramProfileTabSelected(rootNode: AccessibilityNodeInfo): Boolean {
        val profileNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/profile_tab")
        if (!profileNodes.isNullOrEmpty()) {
            for (node in profileNodes) {
                val selected = node.isSelected
                safeRecycle(node)
                if (selected) return true
            }
        }
        return isBottomNavigationTabSelected(rootNode, listOf("Profile"))
    }

    /**
     * Checks if full-screen Reels player (clips viewer) is visible.
     */
    private fun isInstagramClipsViewerActive(rootNode: AccessibilityNodeInfo): Boolean {
        val viewerIds = listOf(
            "com.instagram.android:id/clips_viewer_view_pager",
            "com.instagram.android:id/clips_video_container"
        )
        for (viewId in viewerIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId) ?: continue
            for (node in nodes) {
                val visible = node.isVisibleToUser
                safeRecycle(node)
                if (visible) {
                    nodes.forEach { if (it !== node) safeRecycle(it) }
                    return true
                }
            }
        }
        return false
    }

    /**
     * Instantly redirects from Reels/Explore to the Home feed by clicking the Home tab.
     */
    private fun redirectToInstagramHomeTab(rootNode: AccessibilityNodeInfo): Boolean {
        // Method 1: Click via direct View ID
        val feedTabNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab")
        if (!feedTabNodes.isNullOrEmpty()) {
            for (node in feedTabNodes) {
                val target = if (node.isClickable) node else node.parent
                val clicked = target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                safeRecycle(node)
                if (target !== node && target != null) safeRecycle(target)
                if (clicked) return true
            }
        }

        // Method 2: Click via content description "Home" in the bottom bar area
        return tryClickBottomTab(rootNode, listOf("Home", "Feed"))
    }

    // =========================================================================
    // YOUTUBE LOGIC
    // =========================================================================

    private fun handleYouTubeEvent(rootNode: AccessibilityNodeInfo) {
        // 1. Check if the bottom "Shorts" tab is selected
        if (isYouTubeShortsTabSelected(rootNode)) {
            val redirected = redirectToYouTubeHomeTab(rootNode)
            if (redirected) {
                incrementBlockedCountAsync()
                Log.d(Constants.LOG_TAG, "Redirected from YouTube Shorts tab to Home")
                return
            }
        }

        // 2. Check if full-screen Shorts player is active
        val currentTime = System.currentTimeMillis()
        val cooldown = viewIdProvider.getBlockCooldownMs()

        if (currentTime - lastBlockTimeMs >= cooldown && isYouTubeShortsPlayerActive(rootNode)) {
            lastBlockTimeMs = currentTime
            if (!redirectToYouTubeHomeTab(rootNode)) {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            incrementBlockedCountAsync()
            Log.d(Constants.LOG_TAG, "Blocked YouTube full-screen Short")
        }
    }

    private fun isYouTubeShortsTabSelected(rootNode: AccessibilityNodeInfo): Boolean {
        val pivotNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/pivot_shorts")
        if (!pivotNodes.isNullOrEmpty()) {
            for (node in pivotNodes) {
                val selected = node.isSelected
                safeRecycle(node)
                if (selected) return true
            }
        }
        return isBottomNavigationTabSelected(rootNode, listOf("Shorts"))
    }

    private fun isYouTubeShortsPlayerActive(rootNode: AccessibilityNodeInfo): Boolean {
        val playerIds = viewIdProvider.getYouTubeShortsViewIds()
        for (viewId in playerIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId) ?: continue
            for (node in nodes) {
                val isVisible = node.isVisibleToUser
                val bounds = Rect().also { node.getBoundsInScreen(it) }
                // Only consider full-screen players (> 600px height) to avoid home feed carousels
                val isFullScreen = bounds.height() > 600
                safeRecycle(node)
                if (isVisible && isFullScreen) {
                    nodes.forEach { if (it !== node) safeRecycle(it) }
                    return true
                }
            }
        }
        return false
    }

    private fun redirectToYouTubeHomeTab(rootNode: AccessibilityNodeInfo): Boolean {
        val homeNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/pivot_home")
        if (!homeNodes.isNullOrEmpty()) {
            for (node in homeNodes) {
                val target = if (node.isClickable) node else node.parent
                val clicked = target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                safeRecycle(node)
                if (target !== node && target != null) safeRecycle(target)
                if (clicked) return true
            }
        }
        return tryClickBottomTab(rootNode, listOf("Home"))
    }

    // =========================================================================
    // GENERAL BOTTOM NAVIGATION HELPERS
    // =========================================================================

    /**
     * Checks if a tab matching the given descriptions is currently selected
     * within the bottom area of the screen (bottom 20%).
     */
    private fun isBottomNavigationTabSelected(
        rootNode: AccessibilityNodeInfo,
        tabDescriptions: List<String>
    ): Boolean {
        val screenBounds = Rect().also { rootNode.getBoundsInScreen(it) }
        val bottomThreshold = screenBounds.height() * 0.75 // Bottom quarter of screen

        for (desc in tabDescriptions) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(desc) ?: continue
            for (node in nodes) {
                val nodeBounds = Rect().also { node.getBoundsInScreen(it) }
                val isAtBottom = nodeBounds.top >= bottomThreshold
                val isSelected = node.isSelected || (node.parent?.isSelected == true)

                safeRecycle(node)
                if (isAtBottom && isSelected) {
                    nodes.forEach { if (it !== node) safeRecycle(it) }
                    return true
                }
            }
        }
        return false
    }

    /**
     * Attempts to find and click a tab in the bottom navigation bar.
     */
    private fun tryClickBottomTab(
        rootNode: AccessibilityNodeInfo,
        tabDescriptions: List<String>
    ): Boolean {
        val screenBounds = Rect().also { rootNode.getBoundsInScreen(it) }
        val bottomThreshold = screenBounds.height() * 0.75

        for (desc in tabDescriptions) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(desc) ?: continue
            for (node in nodes) {
                val nodeBounds = Rect().also { node.getBoundsInScreen(it) }
                if (nodeBounds.top >= bottomThreshold && node.isVisibleToUser) {
                    val target = if (node.isClickable) node else node.parent
                    if (target != null && target.isClickable) {
                        val clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        safeRecycle(node)
                        if (target !== node) safeRecycle(target)
                        if (clicked) {
                            nodes.forEach { if (it !== node) safeRecycle(it) }
                            return true
                        }
                    }
                }
                safeRecycle(node)
            }
        }
        return false
    }

    private fun incrementBlockedCountAsync() {
        triggerHapticFeedback()
        serviceScope.launch {
            preferencesRepository.incrementBlockedCount()
        }
    }

    private fun triggerHapticFeedback() {
        if (!currentPreferences.hapticFeedbackEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun safeRecycle(node: AccessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            try {
                node.recycle()
            } catch (_: IllegalStateException) {}
        }
    }

    override fun onInterrupt() {
        Log.w(Constants.LOG_TAG, "BlockerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(Constants.LOG_TAG, "BlockerAccessibilityService destroyed")
    }
}
