package com.reelblocker.app.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.reelblocker.app.ui.analytics.AnalyticsScreen

/**
 * DashboardScreen displays in-depth habit analytics, reclaimed time metrics,
 * 7-day clean rate bar chart, and hourly vulnerability distribution.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    AnalyticsScreen(viewModel = viewModel, modifier = modifier)
}
