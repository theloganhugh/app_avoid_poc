package com.reelblocker.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelblocker.app.data.UserPreferencesRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Onboarding flow.
 * Handles marking onboarding as complete in DataStore.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    /**
     * Mark onboarding as complete and mark disclosure as accepted so neither is shown again.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingComplete()
            repository.setDisclosureAccepted()
        }
    }
}
