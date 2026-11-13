package com.reelblocker.app.util

/**
 * App-wide constants for the ReelBlocker application.
 */
object Constants {
    /** Notification channel ID for the persistent service notification. */
    const val NOTIFICATION_CHANNEL_ID = "blocker_service"

    /** Notification channel name shown in system settings. */
    const val NOTIFICATION_CHANNEL_NAME = "Content Blocker Service"

    /** Tag for logging. */
    const val LOG_TAG = "ReelBlocker"

    /** Target package names. */
    const val PACKAGE_YOUTUBE = "com.google.android.youtube"
    const val PACKAGE_INSTAGRAM = "com.instagram.android"
}
