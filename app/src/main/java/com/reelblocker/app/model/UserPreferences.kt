package com.reelblocker.app.model

import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Daily snapshot representing clean utility usage vs doomscroll intercepts.
 */
data class DayStat(
    val dateStr: String,
    val dayLabel: String,
    val totalSessions: Int,
    val cleanSessions: Int,
    val interceptedAttempts: Int,
    val isToday: Boolean
) {
    /** Percentage of visits that were clean and intentional (0–100%). */
    val cleanRate: Int
        get() = if (totalSessions > 0) {
            ((cleanSessions.toFloat() / totalSessions.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

    /** True if user actively used Instagram/YouTube but had ZERO reel cravings/attempts. */
    val isZeroUrgeDay: Boolean
        get() = totalSessions > 0 && interceptedAttempts == 0

    val estimatedMinutesSaved: Int
        get() = (cleanSessions * 12) + (interceptedAttempts * 2.5).toInt()
}

/**
 * Hourly vulnerability slice for Analytics.
 */
data class HourlyWindowStat(
    val windowName: String,
    val timeRange: String,
    val count: Int,
    val percentage: Int
)

/**
 * Represents user preferences, persistent weekly stats, and focus schedule.
 */
data class UserPreferences(
    val masterToggle: Boolean = false,
    val blockInstagramReels: Boolean = true,
    val blockInstagramExplore: Boolean = true,
    val blockYouTubeShorts: Boolean = true,
    // Daily Activity Metrics
    val blockedCountToday: Int = 0,   // Intercepted attempts today
    val sessionsToday: Int = 0,       // Total app visits today
    val cleanSessionsToday: Int = 0,  // Visited for DMs/Home and exited without doomscrolling
    val lastCounterResetDate: String = "",
    val onboardingComplete: Boolean = false,
    val disclosureAccepted: Boolean = false,
    val snoozeUntilMs: Long = 0L,
    val hapticFeedbackEnabled: Boolean = true,
    // 14-day persistent history JSON
    val weeklyHistoryJson: String = "{}",
    // Hourly distribution JSON (0..23 -> count)
    val hourlyDistributionJson: String = "{}",
    // Scheduled Focus Hours
    val scheduleEnabled: Boolean = false,
    val scheduleStartHour: Int = 9,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = 17,
    val scheduleEndMinute: Int = 0,
    val scheduleDays: Set<Int> = setOf(1, 2, 3, 4, 5) // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri
) {
    /** True if protection is currently paused via a temporary snooze pass. */
    val isSnoozed: Boolean
        get() = snoozeUntilMs > System.currentTimeMillis()

    /** Today's Clean Session Rate (0–100%). */
    val todayCleanRate: Int
        get() = if (sessionsToday > 0) {
            ((cleanSessionsToday.toFloat() / sessionsToday.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

    /** True if the user used Instagram/YouTube today with zero reel urges. */
    val isTodayZeroUrge: Boolean
        get() = sessionsToday > 0 && blockedCountToday == 0

    /**
     * Today's Reclaimed Time (Clean Sessions × 12 min + Intercepts × 2.5 min).
     */
    val estimatedMinutesSaved: Int
        get() = (cleanSessionsToday * 12) + (blockedCountToday * 2.5).toInt()

    /**
     * Focus Level / Rank based on Clean Rate and consistency.
     */
    val focusRank: String
        get() = when {
            averageWeeklyCleanRate >= 95 -> "Zen Master 🧘"
            averageWeeklyCleanRate >= 85 -> "Focus Warrior 🛡️"
            averageWeeklyCleanRate >= 70 -> "Mindful Explorer 🌿"
            else -> "Dopamine Resetting ⚡"
        }

    /**
     * Checks if current time is within scheduled focus hours on an active day.
     */
    fun isCurrentlyInScheduledHours(): Boolean {
        if (!scheduleEnabled) return false
        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek.value
        if (dayOfWeek !in scheduleDays) return false

        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = scheduleStartHour * 60 + scheduleStartMinute
        val endMinutes = scheduleEndHour * 60 + scheduleEndMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    /**
     * Duration in hours of the scheduled focus window (e.g. 8 hours for 9 AM - 5 PM).
     */
    val scheduledDurationHours: Float
        get() {
            val startMinutes = scheduleStartHour * 60 + scheduleStartMinute
            val endMinutes = scheduleEndHour * 60 + scheduleEndMinute
            val totalMinutes = if (startMinutes <= endMinutes) {
                endMinutes - startMinutes
            } else {
                (24 * 60 - startMinutes) + endMinutes
            }
            return (totalMinutes / 60f)
        }

    /**
     * True if protection is effectively active either via master toggle or active schedule.
     */
    val isProtectionActive: Boolean
        get() = (masterToggle || isCurrentlyInScheduledHours()) && !isSnoozed

    /**
     * Generates chronological 7-day stats ending with today.
     */
    fun getWeeklyStats(): List<DayStat> {
        val historyMap = parseHistoryMap(weeklyHistoryJson)
        val today = LocalDate.now()
        val list = mutableListOf<DayStat>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isToday = (i == 0)

            val (sessions, clean, intercepts) = if (isToday) {
                Triple(sessionsToday, cleanSessionsToday, blockedCountToday)
            } else {
                historyMap[dateStr] ?: Triple(0, 0, 0)
            }

            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            list.add(
                DayStat(
                    dateStr = dateStr,
                    dayLabel = dayLabel,
                    totalSessions = sessions,
                    cleanSessions = clean,
                    interceptedAttempts = intercepts,
                    isToday = isToday
                )
            )
        }
        return list
    }

    val totalWeeklyMinutesSaved: Int
        get() = getWeeklyStats().sumOf { it.estimatedMinutesSaved }

    val totalWeeklyIntercepts: Int
        get() = getWeeklyStats().sumOf { it.interceptedAttempts }

    val totalWeeklyZeroUrgeDays: Int
        get() = getWeeklyStats().count { it.isZeroUrgeDay }

    val averageWeeklyCleanRate: Int
        get() {
            val stats = getWeeklyStats().filter { it.totalSessions > 0 }
            if (stats.isEmpty()) return 100
            return (stats.sumOf { it.cleanRate } / stats.size)
        }

    /**
     * Groups hourly intercepts into 4 primary windows:
     * - Morning (6 AM – 12 PM)
     * - Afternoon (12 PM – 5 PM)
     * - Evening (5 PM – 10 PM)
     * - Night (10 PM – 6 AM)
     */
    fun getHourlyVulnerabilityStats(): List<HourlyWindowStat> {
        val hourlyMap = parseHourlyMap(hourlyDistributionJson)
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0

        for (hour in 0..23) {
            val count = hourlyMap[hour] ?: 0
            when (hour) {
                in 6..11 -> morning += count
                in 12..16 -> afternoon += count
                in 17..21 -> evening += count
                else -> night += count
            }
        }

        val total = (morning + afternoon + evening + night).coerceAtLeast(1)
        return listOf(
            HourlyWindowStat("Morning", "6 AM – 12 PM", morning, (morning * 100 / total)),
            HourlyWindowStat("Afternoon", "12 PM – 5 PM", afternoon, (afternoon * 100 / total)),
            HourlyWindowStat("Evening", "5 PM – 10 PM", evening, (evening * 100 / total)),
            HourlyWindowStat("Night", "10 PM – 6 AM", night, (night * 100 / total))
        )
    }

    private fun parseHourlyMap(jsonString: String): Map<Int, Int> {
        if (jsonString.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<Int, Int>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key.toInt()] = json.optInt(key, 0)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun parseHistoryMap(jsonString: String): Map<String, Triple<Int, Int, Int>> {
        if (jsonString.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonString)
            val map = mutableMapOf<String, Triple<Int, Int, Int>>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val dayObj = json.optJSONObject(key)
                if (dayObj != null) {
                    val sessions = dayObj.optInt("sessions", 0)
                    val clean = dayObj.optInt("clean", 0)
                    val intercepts = dayObj.optInt("intercepts", 0)
                    map[key] = Triple(sessions, clean, intercepts)
                } else {
                    val rawCount = json.optInt(key, 0)
                    map[key] = Triple(rawCount, rawCount, 0)
                }
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
