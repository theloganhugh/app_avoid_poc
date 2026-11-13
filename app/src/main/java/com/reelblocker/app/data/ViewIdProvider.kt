package com.reelblocker.app.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.json.JSONArray

/**
 * Provides the list of target View IDs for detecting Reels and Shorts players.
 *
 * Reads from Firebase Remote Config for dynamic updates. Falls back to
 * hardcoded defaults if Remote Config is unavailable or returns empty values.
 */
class ViewIdProvider(
    private val remoteConfigProvider: (() -> FirebaseRemoteConfig)? = null
) {

    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        remoteConfigProvider?.invoke() ?: run {
            try {
                FirebaseRemoteConfig.getInstance()
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        // Hardcoded fallback View IDs (used when Remote Config is unavailable)
        val DEFAULT_YOUTUBE_SHORTS_IDS = listOf(
            "com.google.android.youtube:id/reel_recycler",
            "com.google.android.youtube:id/reel_player_page_container",
            "com.google.android.youtube:id/shorts_player_container"
        )

        val DEFAULT_INSTAGRAM_REELS_IDS = listOf(
            "com.instagram.android:id/clips_viewer_view_pager",
            "com.instagram.android:id/clips_video_container"
        )

        const val DEFAULT_COOLDOWN_MS = 800L

        // Remote Config keys
        const val KEY_YOUTUBE_IDS = "youtube_shorts_view_ids"
        const val KEY_INSTAGRAM_IDS = "instagram_reels_view_ids"
        const val KEY_COOLDOWN = "block_cooldown_ms"
        const val KEY_KILL_SWITCH = "kill_switch"
    }

    /**
     * Get the current list of YouTube Shorts player View IDs.
     * Reads from Remote Config, falls back to hardcoded defaults.
     */
    fun getYouTubeShortsViewIds(): List<String> {
        val configStr = remoteConfig?.getString(KEY_YOUTUBE_IDS) ?: ""
        return parseJsonArray(configStr).ifEmpty { DEFAULT_YOUTUBE_SHORTS_IDS }
    }

    /**
     * Get the current list of Instagram Reels player View IDs.
     * Reads from Remote Config, falls back to hardcoded defaults.
     */
    fun getInstagramReelsViewIds(): List<String> {
        val configStr = remoteConfig?.getString(KEY_INSTAGRAM_IDS) ?: ""
        return parseJsonArray(configStr).ifEmpty { DEFAULT_INSTAGRAM_REELS_IDS }
    }

    /**
     * Get the configured cooldown period between block actions (in milliseconds).
     */
    fun getBlockCooldownMs(): Long {
        val value = remoteConfig?.getLong(KEY_COOLDOWN) ?: 0L
        return if (value > 0) value else DEFAULT_COOLDOWN_MS
    }

    /**
     * Check if the remote kill switch is activated.
     * When true, all blocking should be suspended.
     */
    fun isKillSwitchActive(): Boolean {
        return remoteConfig?.getBoolean(KEY_KILL_SWITCH) ?: false
    }

    /**
     * Parse a JSON array string into a list of strings.
     * Returns empty list on parse failure.
     */
    fun parseJsonArray(jsonString: String): List<String> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
