package com.reelblocker.app.util

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.reelblocker.app.BuildConfig

/**
 * Debug utility that dumps the full AccessibilityNodeInfo tree to Logcat.
 *
 * This is invaluable for discovering actual View IDs used by Instagram and YouTube
 * on real devices. Only active in debug builds — completely no-ops in release.
 *
 * Usage from BlockerAccessibilityService:
 * ```
 * DebugNodeLogger.dumpNodeTree(rootInActiveWindow, "com.instagram.android")
 * ```
 */
object DebugNodeLogger {

    private const val TAG = "NodeTreeDump"
    private const val MAX_DEPTH = 20 // Safety limit to prevent infinite recursion

    /**
     * Dump the complete node tree starting from [rootNode] to Logcat.
     * Only executes in debug builds.
     *
     * @param rootNode The root AccessibilityNodeInfo to start traversal from.
     * @param packageFilter Optional package name to filter — only logs if root matches.
     */
    fun dumpNodeTree(rootNode: AccessibilityNodeInfo?, packageFilter: String? = null) {
        if (!BuildConfig.DEBUG) return
        if (rootNode == null) {
            Log.d(TAG, "Root node is null — nothing to dump")
            return
        }

        // Optional package filter
        if (packageFilter != null && rootNode.packageName?.toString() != packageFilter) {
            return
        }

        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "NODE TREE DUMP — Package: ${rootNode.packageName}")
        Log.d(TAG, "═══════════════════════════════════════════")

        traverseNode(rootNode, depth = 0)

        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "END OF TREE DUMP")
        Log.d(TAG, "═══════════════════════════════════════════")
    }

    /**
     * Recursively traverse and log each node in the hierarchy.
     */
    private fun traverseNode(node: AccessibilityNodeInfo, depth: Int) {
        if (depth > MAX_DEPTH) {
            Log.w(TAG, "${"  ".repeat(depth)}[MAX DEPTH REACHED]")
            return
        }

        val indent = "  ".repeat(depth)
        val viewId = node.viewIdResourceName ?: "(no-id)"
        val className = node.className?.toString() ?: "(no-class)"
        val text = node.text?.toString()?.take(50) ?: ""
        val contentDesc = node.contentDescription?.toString()?.take(50) ?: ""
        val visible = if (node.isVisibleToUser) "V" else "H"
        val clickable = if (node.isClickable) "C" else "-"
        val bounds = android.graphics.Rect().also { node.getBoundsInScreen(it) }

        val logLine = buildString {
            append("${indent}[$visible$clickable] ")
            append("$className ")
            append("id=$viewId ")
            if (text.isNotEmpty()) append("text=\"$text\" ")
            if (contentDesc.isNotEmpty()) append("desc=\"$contentDesc\" ")
            append("bounds=$bounds")
        }

        Log.d(TAG, logLine)

        // Traverse children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, depth + 1)
        }
    }

    /**
     * Log a summary of nodes matching a specific View ID pattern.
     * Useful for quickly checking if a known ID exists in the current tree.
     *
     * @param rootNode The root node to search from.
     * @param viewIdPattern Partial view ID to search for (e.g., "reel" or "shorts").
     */
    fun findNodesMatching(rootNode: AccessibilityNodeInfo?, viewIdPattern: String) {
        if (!BuildConfig.DEBUG) return
        if (rootNode == null) return

        Log.d(TAG, "--- Searching for nodes matching: '$viewIdPattern' ---")
        searchNodes(rootNode, viewIdPattern.lowercase(), depth = 0)
        Log.d(TAG, "--- End search ---")
    }

    private fun searchNodes(node: AccessibilityNodeInfo, pattern: String, depth: Int) {
        if (depth > MAX_DEPTH) return

        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""

        if (viewId.contains(pattern) || contentDesc.contains(pattern) || text.contains(pattern)) {
            val indent = "  ".repeat(depth)
            Log.d(TAG, "${indent}MATCH: id=${node.viewIdResourceName} " +
                    "class=${node.className} " +
                    "text=\"${node.text?.toString()?.take(50)}\" " +
                    "desc=\"${node.contentDescription?.toString()?.take(50)}\" " +
                    "visible=${node.isVisibleToUser}")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            searchNodes(child, pattern, depth + 1)
        }
    }
}
