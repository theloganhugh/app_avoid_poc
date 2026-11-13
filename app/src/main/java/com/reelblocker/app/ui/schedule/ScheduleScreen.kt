package com.reelblocker.app.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelblocker.app.ui.dashboard.DashboardViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Focus Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Automated Habit Boundaries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (preferences.scheduleEnabled) {
                        if (isCurrentlyActive) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = if (!preferences.scheduleEnabled) "DISABLED" else if (isCurrentlyActive) "LIVE NOW" else "SCHEDULED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (preferences.scheduleEnabled) {
                            if (isCurrentlyActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === 1. MASTER SCHEDULE SWITCH CARD ===
            ScheduleMasterCard(
                scheduleEnabled = preferences.scheduleEnabled,
                isCurrentlyActive = isCurrentlyActive,
                startHour = preferences.scheduleStartHour,
                startMinute = preferences.scheduleStartMinute,
                endHour = preferences.scheduleEndHour,
                endMinute = preferences.scheduleEndMinute,
                onToggle = { viewModel.setScheduleEnabled(it) }
            )

            // === 2. CIRCULAR FOCUS TIME DIAL (APPLE BEDTIME STYLE) ===
            CircularFocusScheduleCard(
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

            // === 3. DAY SELECTOR (MINIMALIST PILLS: mo tu we th fr sa su) ===
            CircularDaysCard(
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
    val activeColor = MaterialTheme.colorScheme.tertiary
    val scheduledColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outline

    val accentColor = when {
        !scheduleEnabled -> inactiveColor
        isCurrentlyActive -> activeColor
        else -> scheduledColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(accentColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.1f))
            )
        )
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
                            .border(1.5.dp, accentColor.copy(alpha = 0.4f), CircleShape),
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
                            text = if (scheduleEnabled) "Automated Schedule" else "Schedule Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (scheduleEnabled) {
                                if (isCurrentlyActive) "Focus locked until ${formatHourMinute(endHour, endMinute)}"
                                else "Next window: ${formatHourMinute(startHour, startMinute)}"
                            } else "Enable to automate your focus hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = scheduleEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = accentColor
                    )
                )
            }

            if (scheduleEnabled && isCurrentlyActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(activeColor.copy(alpha = 0.12f))
                        .border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Focus Shield is currently active on your phone.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. CIRCULAR FOCUS TIME DIAL (APPLE BEDTIME STYLE)
// =============================================================================

@Composable
private fun CircularFocusScheduleCard(
    enabled: Boolean,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    durationHours: Float,
    onTimeChange: (Int, Int, Int, Int) -> Unit
) {
    // Mode for precision stepper: 0 = adjust Start, 1 = adjust End
    var activeAdjustTarget by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // === CIRCULAR DIAL ===
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Compose Canvas for Background Track, Active Arc, and Tick Marks
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 22.dp.toPx()
                    val diameter = size.minDimension - strokeWidthPx
                    val radius = diameter / 2f
                    val arcTopLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)

                    // 1. Inactive Background Track (Light Gray)
                    drawArc(
                        color = Color(0xFFF1F5F9),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Calculate Angles for 24-hour dial (00:00 at -90 deg / Top)
                    val startAngle = -90f + ((startHour * 60 + startMinute) / (24f * 60f)) * 360f
                    val endAngle = -90f + ((endHour * 60 + endMinute) / (24f * 60f)) * 360f
                    val sweepAngle = if (endAngle >= startAngle) {
                        endAngle - startAngle
                    } else {
                        (360f - startAngle) + endAngle
                    }

                    // 2. Active Focus Arc (Dark Sleek Slate #1E293B)
                    drawArc(
                        color = if (enabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // 3. Bezel Tick Marks along the active arc
                    val tickCount = (sweepAngle / 4.5f).toInt().coerceAtLeast(1)
                    for (i in 0..tickCount) {
                        val angleDeg = startAngle + (i * 4.5f)
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val rInner = radius - (strokeWidthPx / 2f) + 4.dp.toPx()
                        val rOuter = radius + (strokeWidthPx / 2f) - 4.dp.toPx()

                        val x1 = center.x + (rInner * cos(angleRad)).toFloat()
                        val y1 = center.y + (rInner * sin(angleRad)).toFloat()
                        val x2 = center.x + (rOuter * cos(angleRad)).toFloat()
                        val y2 = center.y + (rOuter * sin(angleRad)).toFloat()

                        drawLine(
                            color = Color.White.copy(alpha = 0.28f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 1.4.dp.toPx()
                        )
                    }

                    // 4. Start Knob Indicator (Bedtime / Shield)
                    val startRad = Math.toRadians(startAngle.toDouble())
                    val startKnobCenter = Offset(
                        center.x + (radius * cos(startRad)).toFloat(),
                        center.y + (radius * sin(startRad)).toFloat()
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 16.dp.toPx(),
                        center = startKnobCenter
                    )
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 16.dp.toPx(),
                        center = startKnobCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 5. End Knob Indicator (Sun / Wake up)
                    val endRad = Math.toRadians(endAngle.toDouble())
                    val endKnobCenter = Offset(
                        center.x + (radius * cos(endRad)).toFloat(),
                        center.y + (radius * sin(endRad)).toFloat()
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 16.dp.toPx(),
                        center = endKnobCenter
                    )
                    drawCircle(
                        color = Color(0xFFEAB308),
                        radius = 16.dp.toPx(),
                        center = endKnobCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Inner Typography Stack (Centered in the Dial)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Start Time (Clickable)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (activeAdjustTarget == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable(enabled = enabled) { activeAdjustTarget = 0 }
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "focus start",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = String.format("%02d:%02d", startHour, startMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeAdjustTarget == 0) MaterialTheme.colorScheme.primary else Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Duration Label
                    Text(
                        text = "${String.format("%.1f", durationHours)} hours of focus",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // End Time (Clickable)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (activeAdjustTarget == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable(enabled = enabled) { activeAdjustTarget = 1 }
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", endHour, endMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeAdjustTarget == 1) MaterialTheme.colorScheme.primary else Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.WbSunny,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFFEAB308)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "focus end",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Precision Stepper Row for Currently Selected Target (Start or End)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (activeAdjustTarget == 0) "Adjusting Start Time" else "Adjusting End Time",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // -1 Hour
                    OutlinedButton(
                        onClick = {
                            if (activeAdjustTarget == 0) {
                                val newH = if (startHour <= 0) 23 else startHour - 1
                                onTimeChange(newH, startMinute, endHour, endMinute)
                            } else {
                                val newH = if (endHour <= 0) 23 else endHour - 1
                                onTimeChange(startHour, startMinute, newH, endMinute)
                            }
                        },
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("-1h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // -15 Min
                    OutlinedButton(
                        onClick = {
                            if (activeAdjustTarget == 0) {
                                val newM = if (startMinute < 15) 45 else startMinute - 15
                                onTimeChange(startHour, newM, endHour, endMinute)
                            } else {
                                val newM = if (endMinute < 15) 45 else endMinute - 15
                                onTimeChange(startHour, startMinute, endHour, newM)
                            }
                        },
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("-15m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // +15 Min
                    OutlinedButton(
                        onClick = {
                            if (activeAdjustTarget == 0) {
                                val newM = if (startMinute >= 45) 0 else startMinute + 15
                                onTimeChange(startHour, newM, endHour, endMinute)
                            } else {
                                val newM = if (endMinute >= 45) 0 else endMinute + 15
                                onTimeChange(startHour, startMinute, endHour, newM)
                            }
                        },
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+15m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // +1 Hour
                    OutlinedButton(
                        onClick = {
                            if (activeAdjustTarget == 0) {
                                val newH = if (startHour >= 23) 0 else startHour + 1
                                onTimeChange(newH, startMinute, endHour, endMinute)
                            } else {
                                val newH = if (endHour >= 23) 0 else endHour + 1
                                onTimeChange(startHour, startMinute, newH, endMinute)
                            }
                        },
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+1h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. MINIMALIST DAYS SELECTOR (EXACT STYLE: mo tu we th fr sa su)
// =============================================================================

@Composable
private fun CircularDaysCard(
    enabled: Boolean,
    scheduleDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
    onSelectPresetDays: (Set<Int>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Active Days",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days row in exact Image 2 style: mo tu we th fr sa su
            val days = listOf(
                1 to "mo",
                2 to "tu",
                3 to "we",
                4 to "th",
                5 to "fr",
                6 to "sa",
                7 to "su"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEach { (dayInt, label) ->
                    val isSelected = scheduleDays.contains(dayInt)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF0F172A)
                                else Color(0xFFF1F5F9)
                            )
                            .clickable(enabled = enabled) { onToggleDay(dayInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 12.sp
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Weekdays (mo-fr)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { onSelectPresetDays(setOf(1, 2, 3, 4, 5, 6, 7)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Everyday (7 days)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val presets = listOf(
                Triple("Workday (09:00 - 17:00)", "Standard 8-hour workday focus", listOf(9, 0, 17, 0)),
                Triple("Deep Work (10:00 - 13:00)", "High-intensity morning block", listOf(10, 0, 13, 0)),
                Triple("Night Guard (22:00 - 07:00)", "No midnight doomscrolling", listOf(22, 0, 7, 0)),
                Triple("All Day Shield (08:00 - 22:00)", "Comprehensive daytime guard", listOf(8, 0, 22, 0))
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
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
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
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        )
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
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Willpower on Autopilot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Scheduled blocks eliminate decision fatigue. Your brain doesn't waste willpower deciding whether to scroll.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
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
