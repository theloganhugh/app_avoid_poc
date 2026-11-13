package com.reelblocker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reelblocker.app.ui.dashboard.DashboardViewModel
import com.reelblocker.app.ui.main.MainScreen
import com.reelblocker.app.ui.onboarding.OnboardingScreen
import com.reelblocker.app.ui.onboarding.OnboardingViewModel

/**
 * Root-level navigation composable.
 * Routes to Onboarding (first launch) or MainScreen (3-page bottom navigation)
 * based on the onboardingComplete flag from DataStore.
 */
@Composable
fun AppNavigation() {
    val dashboardViewModel: DashboardViewModel = viewModel()
    val onboardingViewModel: OnboardingViewModel = viewModel()
    val preferences by dashboardViewModel.preferences.collectAsState()

    if (!preferences.onboardingComplete) {
        OnboardingScreen(
            onComplete = {
                onboardingViewModel.completeOnboarding()
            }
        )
    } else {
        MainScreen(viewModel = dashboardViewModel)
    }
}
