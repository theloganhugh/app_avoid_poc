package com.reelblocker.app.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelblocker.app.ui.components.ProminentDisclosureDialog
import com.reelblocker.app.ui.dashboard.DashboardViewModel
import com.reelblocker.app.ui.theme.ColorDeepHoneyOchre
import com.reelblocker.app.ui.theme.ColorHoneyOchre
import com.reelblocker.app.ui.theme.ColorHoneyOchreWash
import com.reelblocker.app.ui.theme.ColorMatteCharcoal
import com.reelblocker.app.ui.theme.ColorSoftSand
import com.reelblocker.app.ui.theme.ColorSoftSandWash
import com.reelblocker.app.ui.theme.ColorTerracotta
import com.reelblocker.app.ui.theme.ColorTerracottaWash
import com.reelblocker.app.ui.theme.ColorWarmOffWhite
import com.reelblocker.app.util.BatteryOptimizationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val isBatteryOptimized by viewModel.isBatteryOptimized.collectAsState()
    val isKillSwitchActive by viewModel.isKillSwitchActive.collectAsState()
    val showDisclosure by viewModel.showDisclosureDialog.collectAsState()
    val snoozeRemainingSeconds by viewModel.snoozeRemainingSeconds.collectAsState()
    val context = LocalContext.current

    if (showDisclosure) {
        ProminentDisclosureDialog(
            onAccept = {
                viewModel.acceptDisclosure()
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onDismiss = { viewModel.dismissDisclosureDialog() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmOffWhite)
    ) {
        // WhatsApp-like Header TopBar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                ) {
                    Column {
                        Text(
                            text = "ReelBlocker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "Mindful Social Shield",
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                    }

                    HomeStatusBadge(
                        isAccessibilityEnabled = isAccessibilityEnabled,
                        isProtectionActive = preferences.isProtectionActive,
                        isSnoozed = preferences.isSnoozed,
                        isScheduled = preferences.isCurrentlyInScheduledHours()
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ColorWarmOffWhite
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accessibility Warning Banner if service is disabled
            if (!isAccessibilityEnabled) {
                HomeServiceRequiredCard(onEnableClick = { viewModel.requestEnableAccessibility() })
            }

            // Remote Kill Switch Notice if active
            if (isKillSwitchActive) {
                HomeRemoteKillSwitchBanner()
            }

            // === 1. HERO FOCUS PROTECTION CARD ===
            HeroFocusProtectionCard(
                isProtectionActive = preferences.isProtectionActive,
                isMasterOn = preferences.masterToggle,
                isSnoozed = preferences.isSnoozed,
                isScheduled = preferences.isCurrentlyInScheduledHours(),
                snoozeSecondsRemaining = snoozeRemainingSeconds,
                onToggle = { viewModel.setMasterToggle(it) },
                onSnoozeClick = { viewModel.snooze(5) },
                onResumeClick = { viewModel.cancelSnooze() }
            )

            // === 2. TODAY'S RECLAIMED IMPACT CARD ===
            TodayImpactSnapshotCard(
                cleanRate = preferences.todayCleanRate,
                cleanSessions = preferences.cleanSessionsToday,
                totalSessions = preferences.sessionsToday,
                interceptedAttempts = preferences.blockedCountToday,
                minutesSaved = preferences.estimatedMinutesSaved,
                isZeroUrge = preferences.isTodayZeroUrge
            )

            // === 3. PROTECTED APPS SECTION ===
            Text(
                text = "Protected Apps & Surfaces",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ColorMatteCharcoal,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            // Instagram Protection Card
            InstagramProtectionCard(
                masterEnabled = preferences.isProtectionActive,
                reelsEnabled = preferences.blockInstagramReels,
                exploreEnabled = preferences.blockInstagramExplore,
                onReelsToggle = { viewModel.setBlockInstagramReels(it) },
                onExploreToggle = { viewModel.setBlockInstagramExplore(it) }
            )

            // YouTube Shorts Protection Card
            YouTubeProtectionCard(
                masterEnabled = preferences.isProtectionActive,
                shortsEnabled = preferences.blockYouTubeShorts,
                onShortsToggle = { viewModel.setBlockYouTubeShorts(it) }
            )

            // === 4. TACTILE HAPTIC FEEDBACK ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, ColorSoftSand)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ColorTerracottaWash)
                            .border(1.dp, ColorTerracotta.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Vibration,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = ColorTerracotta
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic Bump on Block",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "Gentle vibration reminder when a reel is deflected",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = preferences.hapticFeedbackEnabled,
                        onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorHoneyOchre,
                            uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.White
                        )
                    )
                }
            }

            // === 5. BATTERY OPTIMIZATION STATUS ===
            if (isBatteryOptimized) {
                HomeBatteryOptimizationCard(
                    onFixClick = {
                        try {
                            context.startActivity(BatteryOptimizationHelper.createBatteryOptimizationIntent(context))
                        } catch (_: Exception) {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =============================================================================
// SUB-COMPONENTS FOR HOME SCREEN
// =============================================================================

@Composable
private fun HeroFocusProtectionCard(
    isProtectionActive: Boolean,
    isMasterOn: Boolean,
    isSnoozed: Boolean,
    isScheduled: Boolean,
    snoozeSecondsRemaining: Long,
    onToggle: (Boolean) -> Unit,
    onSnoozeClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val activeColor = ColorTerracotta
    val snoozeColor = ColorHoneyOchre
    val scheduledColor = ColorHoneyOchre
    val inactiveColor = ColorSoftSand

    val targetColor = when {
        isSnoozed -> snoozeColor
        isProtectionActive && isScheduled -> scheduledColor
        isProtectionActive -> activeColor
        else -> inactiveColor
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 350),
        label = "hero_accent_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorSoftSand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(animatedAccentColor.copy(alpha = 0.15f))
                            .border(1.5.dp, animatedAccentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isSnoozed -> Icons.Filled.HourglassTop
                                isProtectionActive -> Icons.Filled.Shield
                                else -> Icons.Filled.Block
                            },
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = when {
                                isSnoozed -> "Protection Paused"
                                isProtectionActive && isScheduled -> "Scheduled Focus Active"
                                isProtectionActive -> "Protection Active"
                                else -> "Protection Off"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = when {
                                isSnoozed -> "Temporary breather mode"
                                isProtectionActive && isScheduled -> "Focus hours locked in"
                                isProtectionActive -> "Intercepting Reels & Shorts"
                                else -> "Tap switch to guard your focus"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                    }
                }

                Switch(
                    checked = isMasterOn,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorTerracotta,
                        uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }

            // Quick Snooze / Resume Bar
            AnimatedVisibility(visible = isProtectionActive || isSnoozed) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (isSnoozed) {
                        val minutes = snoozeSecondsRemaining / 60
                        val seconds = snoozeSecondsRemaining % 60
                        val timeString = String.format("%02d:%02d", minutes, seconds)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ColorHoneyOchreWash)
                                .border(1.dp, ColorHoneyOchre.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = null,
                                    tint = ColorDeepHoneyOchre,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Snoozed: $timeString remaining",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorDeepHoneyOchre
                                )
                            }

                            Button(
                                onClick = onResumeClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorTerracotta,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text("Resume Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSnoozeClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ColorSoftSand)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = ColorMatteCharcoal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Take a 5-min breather (Quick Snooze)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorMatteCharcoal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayImpactSnapshotCard(
    cleanRate: Int,
    cleanSessions: Int,
    totalSessions: Int,
    interceptedAttempts: Int,
    minutesSaved: Int,
    isZeroUrge: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorSoftSand)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Habit Impact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                }

                if (isZeroUrge) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ColorHoneyOchreWash,
                        border = BorderStroke(1.dp, ColorHoneyOchre.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "⭐ Zero-Urge Mastery",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorDeepHoneyOchre,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Clean Session Rate
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarmOffWhite
                    ),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$cleanRate%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = ColorTerracotta
                        )
                        Text(
                            text = "Clean Rate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "$cleanSessions of $totalSessions visits",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ColorMatteCharcoal.copy(alpha = 0.55f)
                        )
                    }
                }

                // Metric 2: Intercepts Deflected
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarmOffWhite
                    ),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$interceptedAttempts",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = ColorTerracotta
                        )
                        Text(
                            text = "Urges Deflected",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "Reels bounced",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ColorMatteCharcoal.copy(alpha = 0.55f)
                        )
                    }
                }

                // Metric 3: Time Reclaimed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarmOffWhite
                    ),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "~${minutesSaved}m",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = ColorTerracotta
                        )
                        Text(
                            text = "Time Reclaimed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "Saved today",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ColorMatteCharcoal.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramProtectionCard(
    masterEnabled: Boolean,
    reelsEnabled: Boolean,
    exploreEnabled: Boolean,
    onReelsToggle: (Boolean) -> Unit,
    onExploreToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorSoftSand)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Instagram",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "DMs, chats, stories & profile remain 100% accessible",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-toggle 1: Reels Tab & Fullscreen viewer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Block Reels Tab",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Redirects dedicated Reels tab to Home feed (<20ms)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = reelsEnabled && masterEnabled,
                    enabled = masterEnabled,
                    onCheckedChange = onReelsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorHoneyOchre,
                        uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }

            // Sub-toggle 2: Explore Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Block Explore Feed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Redirects Explore search tab to prevent algorithmic rabbit holes",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = exploreEnabled && masterEnabled,
                    enabled = masterEnabled,
                    onCheckedChange = onExploreToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorHoneyOchre,
                        uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun YouTubeProtectionCard(
    masterEnabled: Boolean,
    shortsEnabled: Boolean,
    onShortsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorSoftSand)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "YouTube Shorts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Long-form videos, search, and subscriptions remain intact",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Block Full-Screen Shorts",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Instantly exits infinite vertical scrolling player",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = shortsEnabled && masterEnabled,
                    enabled = masterEnabled,
                    onCheckedChange = onShortsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorHoneyOchre,
                        uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeStatusBadge(
    isAccessibilityEnabled: Boolean,
    isProtectionActive: Boolean,
    isSnoozed: Boolean,
    isScheduled: Boolean
) {
    val (label, bg, fg) = when {
        !isAccessibilityEnabled -> Triple("DISABLED", ColorTerracottaWash, ColorTerracotta)
        isSnoozed -> Triple("SNOOZED", ColorHoneyOchreWash, ColorDeepHoneyOchre)
        isProtectionActive && isScheduled -> Triple("SCHEDULED", ColorHoneyOchreWash, ColorDeepHoneyOchre)
        isProtectionActive -> Triple("ACTIVE", ColorTerracottaWash, ColorTerracotta)
        else -> Triple("OFF", ColorSoftSandWash, ColorMatteCharcoal.copy(alpha = 0.6f))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, ColorSoftSand.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun HomeServiceRequiredCard(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorTerracottaWash
        ),
        border = BorderStroke(1.dp, ColorTerracotta.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = ColorTerracotta,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Accessibility Permission Needed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorTerracotta
                )
                Text(
                    text = "Required to detect and deflect short-form video tabs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorMatteCharcoal.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTerracotta,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enable", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeBatteryOptimizationCard(onFixClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorHoneyOchreWash
        ),
        border = BorderStroke(1.dp, ColorHoneyOchre.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.BatteryAlert,
                contentDescription = null,
                tint = ColorDeepHoneyOchre,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Background Protection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorDeepHoneyOchre
                )
                Text(
                    text = "Disable battery optimization so Android doesn't put blocker to sleep",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorMatteCharcoal.copy(alpha = 0.75f)
                )
            }
            TextButton(
                onClick = onFixClick,
                colors = ButtonDefaults.textButtonColors(contentColor = ColorTerracotta)
            ) {
                Text("Fix", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeRemoteKillSwitchBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorTerracottaWash
        ),
        border = BorderStroke(1.dp, ColorTerracotta.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = null,
                tint = ColorTerracotta,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Emergency Kill Switch Active via Remote Config. Protection temporarily paused.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTerracotta
            )
        }
    }
}
