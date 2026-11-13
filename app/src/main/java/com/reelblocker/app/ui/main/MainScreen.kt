package com.reelblocker.app.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.reelblocker.app.ui.analytics.AnalyticsScreen
import com.reelblocker.app.ui.dashboard.DashboardViewModel
import com.reelblocker.app.ui.home.HomeScreen
import com.reelblocker.app.ui.schedule.ScheduleScreen
import com.reelblocker.app.ui.theme.ColorMatteCharcoal
import com.reelblocker.app.ui.theme.ColorSoftSand
import com.reelblocker.app.ui.theme.ColorTerracotta
import com.reelblocker.app.ui.theme.ColorTerracottaWash
import com.reelblocker.app.ui.theme.ColorWarmOffWhite

enum class MainTab(val title: String) {
    HOME("Home"),
    SCHEDULE("Schedule"),
    DASHBOARD("Dashboard")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DashboardViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val preferences by viewModel.preferences.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemStatus()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorWarmOffWhite,
                tonalElevation = 0.dp
            ) {
                // TAB 1: HOME
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Shield else Icons.Outlined.Shield,
                                contentDescription = "Home"
                            )
                            if (!isAccessibilityEnabled || preferences.isProtectionActive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ColorTerracotta)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = "Home",
                            fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorTerracotta,
                        selectedTextColor = ColorTerracotta,
                        unselectedIconColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        unselectedTextColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        indicatorColor = ColorTerracottaWash
                    )
                )

                // TAB 2: SCHEDULE
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.Alarm else Icons.Outlined.Alarm,
                                contentDescription = "Schedule"
                            )
                            if (preferences.scheduleEnabled && preferences.isCurrentlyInScheduledHours()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ColorTerracotta)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = "Schedule",
                            fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorTerracotta,
                        selectedTextColor = ColorTerracotta,
                        unselectedIconColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        unselectedTextColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        indicatorColor = ColorTerracottaWash
                    )
                )

                // TAB 3: DASHBOARD / ANALYTICS
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = {
                        Text(
                            text = "Dashboard",
                            fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorTerracotta,
                        selectedTextColor = ColorTerracotta,
                        unselectedIconColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        unselectedTextColor = ColorMatteCharcoal.copy(alpha = 0.65f),
                        indicatorColor = ColorTerracottaWash
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(viewModel = viewModel)
                    1 -> ScheduleScreen(viewModel = viewModel)
                    2 -> AnalyticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
