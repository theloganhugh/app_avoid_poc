package com.reelblocker.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Helper for managing battery optimization settings.
 * Provides OEM-specific guidance for devices with aggressive battery management.
 */
object BatteryOptimizationHelper {

    /**
     * Check if the app is exempt from battery optimizations.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Create an intent to request battery optimization exemption.
     */
    fun createBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Get OEM-specific instructions for disabling battery optimization.
     * Returns a human-readable string with step-by-step instructions.
     */
    fun getOemSpecificInstructions(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") ->
                "Samsung: Settings → Battery → Background usage limits → Never sleeping apps → Add ReelBlocker"

            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ->
                "Xiaomi: Settings → Apps → Manage apps → ReelBlocker → Battery saver → No restrictions"

            manufacturer.contains("oneplus") ->
                "OnePlus: Settings → Battery → Battery optimization → ReelBlocker → Don't optimize"

            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                "Huawei: Settings → Battery → App launch → ReelBlocker → Manage manually → Enable all toggles"

            manufacturer.contains("oppo") || manufacturer.contains("realme") ->
                "OPPO/Realme: Settings → Battery → More settings → Optimize battery use → ReelBlocker → Don't optimize"

            manufacturer.contains("vivo") ->
                "Vivo: Settings → Battery → Background power consumption management → ReelBlocker → Allow"

            else ->
                "Settings → Battery → Battery optimization → ReelBlocker → Don't optimize"
        }
    }
}
