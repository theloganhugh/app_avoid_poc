package com.reelblocker.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.reelblocker.app.data.RemoteConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for ReelBlocker.
 * Initializes Firebase and triggers Remote Config fetch on app startup.
 */
class ReelBlockerApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            // Asynchronously fetch and activate the latest remote configuration
            applicationScope.launch {
                val remoteConfigRepository = RemoteConfigRepository()
                remoteConfigRepository.fetchAndActivate()
            }
        } catch (_: Exception) {
            // Firebase setup may be absent in testing or without google-services.json
        }
    }
}
