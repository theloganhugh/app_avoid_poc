package com.reelblocker.app.ui.analytics

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelblocker.app.model.DayStat
import com.reelblocker.app.model.HourlyWindowStat
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val weeklyStats = preferences.getWeeklyStats()
    val hourlyStats = preferences.getHourlyVulnerabilityStats()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmOffWhite)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Habit Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Attention & Reclaimed Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
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
            // === 1. FOCUS MASTERY RANK & WEEKLY HIGHLIGHTS ===
            FocusMasteryHeroCard(
                rank = preferences.focusRank,
                cleanRate = preferences.averageWeeklyCleanRate,
                totalMinutes = preferences.totalWeeklyMinutesSaved,
                zeroUrgeDays = preferences.totalWeeklyZeroUrgeDays
            )

            // === 2. GRAPH 1: 7-DAY CLEAN SESSION & RECLAIMED TIME BAR CHART ===
            WeeklyCleanRateBarChartCard(
                weeklyStats = weeklyStats,
                averageCleanRate = preferences.averageWeeklyCleanRate
            )

            // === 3. GRAPH 2: HOURLY DOOMSCROLL VULNERABILITY DISTRIBUTION ===
            HourlyVulnerabilityGraphCard(
                hourlyStats = hourlyStats
            )

            // === 4. GRAPH 3: APP TEMPTATION BREAKDOWN ===
            AppBreakdownCard(
                blockInstagramReels = preferences.blockInstagramReels,
                blockInstagramExplore = preferences.blockInstagramExplore,
                blockYouTubeShorts = preferences.blockYouTubeShorts,
                totalIntercepts = preferences.blockedCountToday
            )

            // === 5. HABIT RECONDITIONING METRICS ===
            HabitReconditioningCard(
                cleanSessionsToday = preferences.cleanSessionsToday,
                sessionsToday = preferences.sessionsToday,
                cleanRate = preferences.todayCleanRate
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// =============================================================================
// 1. FOCUS MASTERY HERO CARD
// =============================================================================

@Composable
private fun FocusMasteryHeroCard(
    rank: String,
    cleanRate: Int,
    totalMinutes: Int,
    zeroUrgeDays: Int
) {
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    val timeFormatted = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ColorTerracottaWash)
                            .border(1.dp, ColorTerracotta.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = ColorTerracotta,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Focus Rank",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorTerracotta
                        )
                        Text(
                            text = rank,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ColorMatteCharcoal
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ColorHoneyOchreWash,
                    border = BorderStroke(1.dp, ColorHoneyOchre.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$cleanRate% Clean",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorDeepHoneyOchre,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 High-Impact KPI Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reclaimed Time
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
                            text = timeFormatted,
                            style = MaterialTheme.typography.titleMedium,
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
                            text = "Past 7 days",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ColorMatteCharcoal.copy(alpha = 0.55f)
                        )
                    }
                }

                // Zero-Urge Mastery Days
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
                            text = "$zeroUrgeDays / 7",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = ColorTerracotta
                        )
                        Text(
                            text = "Zero-Urge Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "⭐ Pure utility",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ColorMatteCharcoal.copy(alpha = 0.55f)
                        )
                    }
                }

                // Average Clean Rate
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = ColorTerracotta
                        )
                        Text(
                            text = "Avg Clean Rate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = "Intentional visits",
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

// =============================================================================
// 2. GRAPH 1: 7-DAY CLEAN SESSION BAR CHART
// =============================================================================

@Composable
private fun WeeklyCleanRateBarChartCard(
    weeklyStats: List<DayStat>,
    averageCleanRate: Int
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
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "7-Day Clean Usage Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                }

                Text(
                    text = "Goal: ≥80%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorDeepHoneyOchre
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Measures intentional visits (DMs, posting, search) vs reflex urges. ⭐ marks days with 0 reel urges.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorMatteCharcoal.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 7-DAY VERTICAL BAR CHART
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { stat ->
                    val cleanRate = stat.cleanRate
                    val isHighlighted = stat.isToday
                    val isZeroUrge = stat.isZeroUrgeDay

                    // Bar height fraction (coerce between 0.12f and 1f so bars always visible)
                    val barHeightFraction = (cleanRate / 100f).coerceIn(0.12f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Zero Urge Star or Clean Rate %
                        if (isZeroUrge) {
                            Text(
                                text = "⭐",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        } else {
                            Text(
                                text = "$cleanRate%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighlighted) ColorTerracotta else ColorMatteCharcoal.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // Bar Pillar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .fillMaxHeight(barHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        when {
                                            isHighlighted -> ColorTerracotta
                                            cleanRate >= 80 -> ColorHoneyOchre
                                            else -> ColorSoftSand
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day of week label
                        Text(
                            text = if (stat.isToday) "Today" else stat.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (stat.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (stat.isToday) ColorTerracotta else ColorMatteCharcoal.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. GRAPH 2: HOURLY DOOMSCROLL VULNERABILITY DISTRIBUTION
// =============================================================================

@Composable
private fun HourlyVulnerabilityGraphCard(
    hourlyStats: List<HourlyWindowStat>
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
                        imageVector = Icons.Filled.Insights,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Doomscroll Vulnerability Clock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorTerracottaWash,
                    border = BorderStroke(1.dp, ColorTerracotta.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Urge Heatmap",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ColorTerracotta,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Shows the times of day when your reflex to doomscroll is triggered most frequently.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorMatteCharcoal.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                hourlyStats.forEach { stat ->
                    val barColor = when (stat.windowName) {
                        "Night" -> ColorTerracotta
                        "Evening" -> ColorHoneyOchre
                        "Afternoon" -> ColorDeepHoneyOchre
                        else -> ColorSoftSand
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stat.windowName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorMatteCharcoal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${stat.timeRange})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ColorMatteCharcoal.copy(alpha = 0.6f)
                                )
                            }

                            Text(
                                text = "${stat.percentage}% (${stat.count} urges)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (stat.percentage / 100f).coerceIn(0.04f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = barColor,
                            trackColor = ColorSoftSandWash,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actionable Focus Recommendation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorHoneyOchreWash)
                    .border(1.dp, ColorHoneyOchre.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = ColorDeepHoneyOchre,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tip: Most users relapse in the Evening & Night. Schedule your Focus Hours from 8 PM to 11 PM to guard your sleep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorDeepHoneyOchre
                )
            }
        }
    }
}

// =============================================================================
// 4. GRAPH 3: APP TEMPTATION BREAKDOWN
// =============================================================================

@Composable
private fun AppBreakdownCard(
    blockInstagramReels: Boolean,
    blockInstagramExplore: Boolean,
    blockYouTubeShorts: Boolean,
    totalIntercepts: Int
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
            Text(
                text = "Surface Temptation Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ColorMatteCharcoal
            )
            Text(
                text = "Where attention leaks occur across your installed apps.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorMatteCharcoal.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Instagram Share Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarmOffWhite
                    ),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
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
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instagram",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorMatteCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Reels & Explore",
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                        Text(
                            text = if (blockInstagramReels) "Shielded 🛡️" else "Unprotected",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (blockInstagramReels) ColorTerracotta else ColorSoftSand
                        )
                    }
                }

                // YouTube Share Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarmOffWhite
                    ),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF0000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "YouTube",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorMatteCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Shorts Player",
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                        Text(
                            text = if (blockYouTubeShorts) "Shielded 🛡️" else "Unprotected",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (blockYouTubeShorts) ColorTerracotta else ColorSoftSand
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. HABIT RECONDITIONING CARD
// =============================================================================

@Composable
private fun HabitReconditioningCard(
    cleanSessionsToday: Int,
    sessionsToday: Int,
    cleanRate: Int
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorTerracottaWash)
                        .border(1.dp, ColorTerracotta.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Habit Reconditioning Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Why clean sessions matter more than raw blocks",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "When you open Instagram to answer a DM or YouTube to learn a skill, and exit without watching a single short-form video, your brain breaks the automatic dopamine loop. That visit is celebrated as a Clean Session.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorMatteCharcoal.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorWarmOffWhite)
                    .border(1.dp, ColorSoftSand, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Today's Clean Habit Score",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorMatteCharcoal
                )
                Text(
                    text = "$cleanSessionsToday clean / $sessionsToday total visits ($cleanRate%)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTerracotta
                )
            }
        }
    }
}
