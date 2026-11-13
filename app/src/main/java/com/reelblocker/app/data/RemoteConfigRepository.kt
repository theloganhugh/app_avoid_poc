package com.reelblocker.app.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.reelblocker.app.BuildConfig
import com.reelblocker.app.R
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing Firebase Remote Config.
 * Handles initialization, fetching, and activation of remote configuration values.
 */
class RemoteConfigRepository {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(
                if (BuildConfig.DEBUG) 0L else 43200L // 0 for debug, 12 hours for release
            )
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    /**
     * Fetch and activate the latest remote config values.
     * Call this on app startup and periodically.
     *
     * @return true if new values were fetched and activated, false otherwise.
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            // Silently fail — hardcoded defaults will be used
            false
        }
    }
}
