package com.reelblocker.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for UserPreferences:
 * - Clean Usage Model & Reclaimed Time calculations
 * - Zero-Urge Day detection
 * - 7-day stats aggregation
 */
class UserPreferencesTest {

    @Test
    fun isProtectionActive_whenMasterOn_returnsTrue() {
        val prefs = UserPreferences(masterToggle = true)
        assertTrue(prefs.isProtectionActive)
    }

    @Test
    fun isProtectionActive_whenMasterOffAndNoSchedule_returnsFalse() {
        val prefs = UserPreferences(masterToggle = false, scheduleEnabled = false)
        assertFalse(prefs.isProtectionActive)
    }

    @Test
    fun isProtectionActive_whenSnoozed_returnsFalseEvenIfMasterOn() {
        val prefs = UserPreferences(
            masterToggle = true,
            snoozeUntilMs = System.currentTimeMillis() + 60_000L
        )
        assertFalse(prefs.isProtectionActive)
    }

    @Test
    fun cleanUsageModel_calculatesTimeSavedFromCleanSessionsAndIntercepts() {
        // User had 5 visits: 4 clean visits, 1 intercepted visit
        val prefs = UserPreferences(
            sessionsToday = 5,
            cleanSessionsToday = 4,
            blockedCountToday = 1
        )

        // Clean rate: 4/5 = 80%
        assertEquals(80, prefs.todayCleanRate)

        // Estimated minutes saved: (4 clean * 12 min) + (1 intercept * 2.5 min) = 48 + 2 = 50 min
        assertEquals(50, prefs.estimatedMinutesSaved)
    }

    @Test
    fun isTodayZeroUrge_whenUserHadCleanVisitsAndZeroUrges_returnsTrue() {
        val prefs = UserPreferences(
            sessionsToday = 6,
            cleanSessionsToday = 6,
            blockedCountToday = 0
        )
        assertTrue(prefs.isTodayZeroUrge)
        assertEquals(100, prefs.todayCleanRate)
        // 6 * 12 min = 72 min saved
        assertEquals(72, prefs.estimatedMinutesSaved)
    }

    @Test
    fun getWeeklyStats_returnsSevenDaysWithTodayMarked() {
        val prefs = UserPreferences(
            sessionsToday = 4,
            cleanSessionsToday = 4,
            blockedCountToday = 0
        )
        val stats = prefs.getWeeklyStats()

        assertEquals(7, stats.size)
        assertTrue(stats.last().isToday)
        assertEquals(4, stats.last().cleanSessions)
        assertTrue(stats.last().isZeroUrgeDay)
    }
}
