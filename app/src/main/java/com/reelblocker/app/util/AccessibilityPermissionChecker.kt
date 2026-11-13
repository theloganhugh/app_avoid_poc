package com.reelblocker.app.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.reelblocker.app.service.BlockerAccessibilityService

/**
 * Utility for checking whether the BlockerAccessibilityService is currently
 * enabled in the device's Accessibility settings.
 */
object AccessibilityPermissionChecker {

    /**
     * Check if [BlockerAccessibilityService] is currently enabled.
     *
     * @param context Application or Activity context.
     * @return true if the service is enabled in Accessibility settings.
     */
    fun isServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, BlockerAccessibilityService::class.java)
            .flattenToString()

        val enabledServices: String = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
        } catch (e: Exception) {
            return false
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            if (componentNameString.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
