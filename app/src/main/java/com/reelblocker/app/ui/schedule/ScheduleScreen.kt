package com.reelblocker.app.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ScheduleScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val isCurrentlyActive = preferences.isCurrentlyInScheduledHours()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmOffWhite)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Focus Scheduler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = ColorMatteCharcoal
                    )
                    Text(
                        text = "Automated Habit Boundaries",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorMatteCharcoal.copy(alpha = 0.65f)
                    )
                }
            },
            actions = {
                val (badgeText, badgeBg, badgeFg) = when {
                    !preferences.scheduleEnabled -> Triple("DISABLED", ColorSoftSandWash, ColorMatteCharcoal.copy(alpha = 0.6f))
                    isCurrentlyActive -> Triple("LIVE NOW", ColorTerracottaWash, ColorTerracotta)
                    else -> Triple("SCHEDULED", ColorHoneyOchreWash, ColorDeepHoneyOchre)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, ColorSoftSand.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
            // === 1. MASTER SCHEDULE CARD ===
            ScheduleMasterCard(
                scheduleEnabled = preferences.scheduleEnabled,
                isCurrentlyActive = isCurrentlyActive,
                startHour = preferences.scheduleStartHour,
                startMinute = preferences.scheduleStartMinute,
                endHour = preferences.scheduleEndHour,
                endMinute = preferences.scheduleEndMinute,
                onToggle = { viewModel.setScheduleEnabled(it) }
            )

            // === 2. DEDICATED IN-PLACE TIME SELECTOR (START & END TIME) ===
            InteractiveTimeSelectorCard(
                enabled = preferences.scheduleEnabled,
                startHour = preferences.scheduleStartHour,
                startMinute = preferences.scheduleStartMinute,
                endHour = preferences.scheduleEndHour,
                endMinute = preferences.scheduleEndMinute,
                durationHours = preferences.scheduledDurationHours,
                onTimeChange = { sH, sM, eH, eM ->
                    viewModel.setScheduleTime(sH, sM, eH, eM)
                }
            )

            // === 3. ACTIVE DAYS OF THE WEEK (M T W T F S S) ===
            ActiveDaysCard(
                enabled = preferences.scheduleEnabled,
                scheduleDays = preferences.scheduleDays,
                onToggleDay = { viewModel.toggleScheduleDay(it) },
                onSelectPresetDays = { days ->
                    days.forEach { day ->
                        if (!preferences.scheduleDays.contains(day)) {
                            viewModel.toggleScheduleDay(day)
                        }
                    }
                }
            )

            // === 4. QUICK PRESETS ===
            SchedulePresetsCard(
                enabled = preferences.scheduleEnabled,
                onSelectPreset = { sH, sM, eH, eM ->
                    viewModel.setScheduleTime(sH, sM, eH, eM)
                }
            )

            // === 5. BEHAVIORAL SCIENCE INSIGHT ===
            FocusScienceCard()

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// =============================================================================
// 1. MASTER SCHEDULE CARD
// =============================================================================

@Composable
private fun ScheduleMasterCard(
    scheduleEnabled: Boolean,
    isCurrentlyActive: Boolean,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onToggle: (Boolean) -> Unit
) {
    val accentColor = when {
        !scheduleEnabled -> ColorSoftSand
        isCurrentlyActive -> ColorTerracotta
        else -> ColorHoneyOchre
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.5.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (scheduleEnabled) "Automated Focus Hours" else "Schedule Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorMatteCharcoal
                        )
                        Text(
                            text = if (scheduleEnabled) {
                                if (isCurrentlyActive) "Focus locked until ${formatHourMinute(endHour, endMinute)}"
                                else "Next window: ${formatHourMinute(startHour, startMinute)}"
                            } else "Enable to automate your focus hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorMatteCharcoal.copy(alpha = 0.65f)
                        )
                    }
                }

                Switch(
                    checked = scheduleEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorTerracotta,
                        uncheckedTrackColor = ColorSoftSand.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }

            if (scheduleEnabled && isCurrentlyActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorTerracottaWash)
                        .border(1.dp, ColorTerracotta.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Focus Shield is currently active on your phone.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTerracotta
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. DEDICATED IN-PLACE TIME SELECTOR (START & END TIME)
// =============================================================================

@Composable
private fun InteractiveTimeSelectorCard(
    enabled: Boolean,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    durationHours: Float,
    onTimeChange: (Int, Int, Int, Int) -> Unit
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
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom Focus Hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorMatteCharcoal
                    )
                }

                // Honey Ochre Duration Capsule
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorHoneyOchreWash,
                    border = BorderStroke(1.dp, ColorHoneyOchre.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${String.format("%.1f", durationHours)} hrs/day",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ColorDeepHoneyOchre,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // START TIME CONTROLLER
            TimeBlockController(
                title = "START TIME",
                titleColor = ColorTerracotta,
                hour = startHour,
                minute = startMinute,
                enabled = enabled,
                onHourChange = { newHour ->
                    onTimeChange(newHour, startMinute, endHour, endMinute)
                },
                onMinuteChange = { newMinute ->
                    onTimeChange(startHour, newMinute, endHour, endMinute)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Flow indicator between Start and End
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ColorTerracottaWash)
                        .border(1.dp, ColorTerracotta.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ColorTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // END TIME CONTROLLER
            TimeBlockController(
                title = "END TIME",
                titleColor = ColorDeepHoneyOchre,
                hour = endHour,
                minute = endMinute,
                enabled = enabled,
                onHourChange = { newHour ->
                    onTimeChange(startHour, startMinute, newHour, endMinute)
                },
                onMinuteChange = { newMinute ->
                    onTimeChange(startHour, startMinute, endHour, newMinute)
                }
            )
        }
    }
}

@Composable
private fun TimeBlockController(
    title: String,
    titleColor: Color,
    hour: Int,
    minute: Int,
    enabled: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val isPm = hour >= 12

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorWarmOffWhite
        ),
        border = BorderStroke(1.dp, ColorSoftSand)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hour Controller: -  09  +
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            var h = hour - 1
                            if (h < 0) h = 23
                            onHourChange(h)
                        },
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease Hour",
                            modifier = Modifier.size(20.dp),
                            tint = ColorMatteCharcoal.copy(alpha = 0.75f)
                        )
                    }

                    Text(
                        text = String.format("%02d", displayHour),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorMatteCharcoal,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    IconButton(
                        onClick = {
                            var h = hour + 1
                            if (h > 23) h = 0
                            onHourChange(h)
                        },
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase Hour",
                            modifier = Modifier.size(20.dp),
                            tint = ColorMatteCharcoal.copy(alpha = 0.75f)
                        )
                    }
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorMatteCharcoal.copy(alpha = 0.45f)
                )

                // Minute Controller: -  00  +
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newMin = if (minute <= 0) 45 else minute - 15
                            onMinuteChange(newMin)
                        },
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease Minute",
                            modifier = Modifier.size(20.dp),
                            tint = ColorMatteCharcoal.copy(alpha = 0.75f)
                        )
                    }

                    Text(
                        text = String.format("%02d", minute),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorMatteCharcoal,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    IconButton(
                        onClick = {
                            val newMin = if (minute >= 45) 0 else minute + 15
                            onMinuteChange(newMin)
                        },
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase Minute",
                            modifier = Modifier.size(20.dp),
                            tint = ColorMatteCharcoal.copy(alpha = 0.75f)
                        )
                    }
                }

                // AM / PM Segmented Pill Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSoftSandWash)
                        .border(1.dp, ColorSoftSand, RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isPm) ColorTerracotta else Color.Transparent)
                            .clickable(enabled = enabled) {
                                if (isPm) onHourChange(hour - 12)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!isPm) Color.White else ColorMatteCharcoal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isPm) ColorTerracotta else Color.Transparent)
                            .clickable(enabled = enabled) {
                                if (!isPm) onHourChange(hour + 12)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPm) Color.White else ColorMatteCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick minute chips (:00, :15, :30, :45)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 15, 30, 45).forEach { m ->
                    val isSelected = (minute == m)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ColorTerracotta else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ColorTerracotta else ColorSoftSand
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = enabled) { onMinuteChange(m) }
                    ) {
                        Text(
                            text = ":${String.format("%02d", m)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else ColorMatteCharcoal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. ACTIVE DAYS OF THE WEEK (M T W T F S S)
// =============================================================================

@Composable
private fun ActiveDaysCard(
    enabled: Boolean,
    scheduleDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
    onSelectPresetDays: (Set<Int>) -> Unit
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
            Text(
                text = "Active Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ColorMatteCharcoal
            )
            Spacer(modifier = Modifier.height(12.dp))

            val days = listOf(
                1 to "M",
                2 to "T",
                3 to "W",
                4 to "T",
                5 to "F",
                6 to "S",
                7 to "S"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { (dayInt, label) ->
                    val isSelected = scheduleDays.contains(dayInt)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) ColorTerracotta // Selected state: Terracotta #E37449
                                else ColorSoftSandWash
                            )
                            .border(
                                1.dp,
                                if (isSelected) ColorTerracotta else ColorSoftSand,
                                CircleShape
                            )
                            .clickable(enabled = enabled) { onToggleDay(dayInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else ColorMatteCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick days filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onSelectPresetDays(setOf(1, 2, 3, 4, 5)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Text("Mon-Fri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorMatteCharcoal)
                }

                OutlinedButton(
                    onClick = { onSelectPresetDays(setOf(1, 2, 3, 4, 5, 6, 7)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorSoftSand)
                ) {
                    Text("Everyday", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorMatteCharcoal)
                }
            }
        }
    }
}

// =============================================================================
// 4. QUICK PRESETS
// =============================================================================

@Composable
private fun SchedulePresetsCard(
    enabled: Boolean,
    onSelectPreset: (Int, Int, Int, Int) -> Unit
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
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ColorMatteCharcoal
            )
            Spacer(modifier = Modifier.height(12.dp))

            val presets = listOf(
                Triple("Workday (9 AM - 5 PM)", "Standard 8-hour workday focus", listOf(9, 0, 17, 0)),
                Triple("Deep Work (10 AM - 1 PM)", "High-intensity morning block", listOf(10, 0, 13, 0)),
                Triple("Night Guard (10 PM - 7 AM)", "No midnight doomscrolling", listOf(22, 0, 7, 0)),
                Triple("All Day Shield (8 AM - 10 PM)", "Comprehensive daytime guard", listOf(8, 0, 22, 0))
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (title, subtitle, times) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) {
                                onSelectPreset(times[0], times[1], times[2], times[3])
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ColorWarmOffWhite
                        ),
                        border = BorderStroke(1.dp, ColorSoftSand)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorMatteCharcoal
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ColorMatteCharcoal.copy(alpha = 0.65f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = ColorTerracotta,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. BEHAVIORAL SCIENCE INSIGHT
// =============================================================================

@Composable
private fun FocusScienceCard() {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ColorHoneyOchre.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = ColorDeepHoneyOchre,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Willpower on Autopilot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorDeepHoneyOchre
                )
                Text(
                    text = "Scheduled blocks eliminate decision fatigue. Your brain doesn't waste willpower deciding whether to scroll.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorMatteCharcoal.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatHourMinute(hour: Int, minute: Int): String {
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    return String.format("%02d:%02d %s", displayHour, minute, amPm)
}
