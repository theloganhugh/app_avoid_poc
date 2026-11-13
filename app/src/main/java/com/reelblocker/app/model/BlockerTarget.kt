package com.reelblocker.app.model

/**
 * Represents the target platforms that the blocker can monitor.
 */
enum class BlockerTarget(val packageName: String, val displayName: String) {
    INSTAGRAM_REELS(
        packageName = "com.instagram.android",
        displayName = "Instagram Reels"
    ),
    YOUTUBE_SHORTS(
        packageName = "com.google.android.youtube",
        displayName = "YouTube Shorts"
    );

    companion object {
        /**
         * Find a BlockerTarget by its Android package name.
         * @return The matching BlockerTarget, or null if not found.
         */
        fun fromPackageName(packageName: String): BlockerTarget? {
            return entries.find { it.packageName == packageName }
        }
    }
}
