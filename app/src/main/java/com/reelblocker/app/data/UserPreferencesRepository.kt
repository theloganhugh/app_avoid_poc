package com.reelblocker.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reelblocker.app.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for reading and writing user preferences via Jetpack DataStore.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val MASTER_TOGGLE = booleanPreferencesKey("master_toggle")
        val BLOCK_INSTAGRAM_REELS = booleanPreferencesKey("block_instagram_reels")
        val BLOCK_INSTAGRAM_EXPLORE = booleanPreferencesKey("block_instagram_explore")
        val BLOCK_YOUTUBE_SHORTS = booleanPreferencesKey("block_youtube_shorts")
        val BLOCKED_COUNT_TODAY = intPreferencesKey("blocked_count_today")
        val SESSIONS_TODAY = intPreferencesKey("sessions_today")
        val CLEAN_SESSIONS_TODAY = intPreferencesKey("clean_sessions_today")
        val LAST_COUNTER_RESET_DATE = stringPreferencesKey("last_counter_reset_date")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val DISCLOSURE_ACCEPTED = booleanPreferencesKey("disclosure_accepted")
        val SNOOZE_UNTIL_MS = longPreferencesKey("snooze_until_ms")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val WEEKLY_HISTORY_JSON = stringPreferencesKey("weekly_history_json")
        val HOURLY_DISTRIBUTION_JSON = stringPreferencesKey("hourly_distribution_json")
        val SCHEDULE_ENABLED = booleanPreferencesKey("schedule_enabled")
        val SCHEDULE_START_HOUR = intPreferencesKey("schedule_start_hour")
        val SCHEDULE_START_MINUTE = intPreferencesKey("schedule_start_minute")
        val SCHEDULE_END_HOUR = intPreferencesKey("schedule_end_hour")
        val SCHEDULE_END_MINUTE = intPreferencesKey("schedule_end_minute")
        val SCHEDULE_DAYS = stringPreferencesKey("schedule_days")
    }

    /**
     * Observe user preferences as a Flow. Emits a new [UserPreferences] whenever
     * any preference value changes. Automatically resets the daily counter at midnight.
     */
    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastResetDate = prefs[PreferenceKeys.LAST_COUNTER_RESET_DATE] ?: ""
        val isNewDay = lastResetDate != todayStr

        val blockedCount = if (isNewDay) 0 else (prefs[PreferenceKeys.BLOCKED_COUNT_TODAY] ?: 0)
        val sessions = if (isNewDay) 0 else (prefs[PreferenceKeys.SESSIONS_TODAY] ?: 0)
        val cleanSessions = if (isNewDay) 0 else (prefs[PreferenceKeys.CLEAN_SESSIONS_TODAY] ?: 0)

        val daysString = prefs[PreferenceKeys.SCHEDULE_DAYS] ?: "1,2,3,4,5"
        val daysSet = daysString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
            .ifEmpty { setOf(1, 2, 3, 4, 5) }

        UserPreferences(
            masterToggle = prefs[PreferenceKeys.MASTER_TOGGLE] ?: false,
            blockInstagramReels = prefs[PreferenceKeys.BLOCK_INSTAGRAM_REELS] ?: true,
            blockInstagramExplore = prefs[PreferenceKeys.BLOCK_INSTAGRAM_EXPLORE] ?: true,
            blockYouTubeShorts = prefs[PreferenceKeys.BLOCK_YOUTUBE_SHORTS] ?: true,
            blockedCountToday = blockedCount,
            sessionsToday = sessions,
            cleanSessionsToday = cleanSessions,
            lastCounterResetDate = if (isNewDay) todayStr else lastResetDate,
            onboardingComplete = prefs[PreferenceKeys.ONBOARDING_COMPLETE] ?: false,
            disclosureAccepted = prefs[PreferenceKeys.DISCLOSURE_ACCEPTED] ?: false,
            snoozeUntilMs = prefs[PreferenceKeys.SNOOZE_UNTIL_MS] ?: 0L,
            hapticFeedbackEnabled = prefs[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] ?: true,
            weeklyHistoryJson = prefs[PreferenceKeys.WEEKLY_HISTORY_JSON] ?: "{}",
            hourlyDistributionJson = prefs[PreferenceKeys.HOURLY_DISTRIBUTION_JSON] ?: "{}",
            scheduleEnabled = prefs[PreferenceKeys.SCHEDULE_ENABLED] ?: false,
            scheduleStartHour = prefs[PreferenceKeys.SCHEDULE_START_HOUR] ?: 9,
            scheduleStartMinute = prefs[PreferenceKeys.SCHEDULE_START_MINUTE] ?: 0,
            scheduleEndHour = prefs[PreferenceKeys.SCHEDULE_END_HOUR] ?: 17,
            scheduleEndMinute = prefs[PreferenceKeys.SCHEDULE_END_MINUTE] ?: 0,
            scheduleDays = daysSet
        )
    }

    suspend fun setMasterToggle(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.MASTER_TOGGLE] = enabled
        }
    }

    suspend fun setBlockInstagramReels(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BLOCK_INSTAGRAM_REELS] = enabled
        }
    }

    suspend fun setBlockInstagramExplore(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BLOCK_INSTAGRAM_EXPLORE] = enabled
        }
    }

    suspend fun setBlockYouTubeShorts(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BLOCK_YOUTUBE_SHORTS] = enabled
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun snooze(durationMinutes: Int) {
        val targetMs = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SNOOZE_UNTIL_MS] = targetMs
        }
    }

    suspend fun cancelSnooze() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SNOOZE_UNTIL_MS] = 0L
        }
    }

    suspend fun setScheduleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SCHEDULE_ENABLED] = enabled
        }
    }

    suspend fun setScheduleTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SCHEDULE_START_HOUR] = startHour
            prefs[PreferenceKeys.SCHEDULE_START_MINUTE] = startMinute
            prefs[PreferenceKeys.SCHEDULE_END_HOUR] = endHour
            prefs[PreferenceKeys.SCHEDULE_END_MINUTE] = endMinute
        }
    }

    suspend fun toggleScheduleDay(dayOfWeek: Int) {
        context.dataStore.edit { prefs ->
            val currentString = prefs[PreferenceKeys.SCHEDULE_DAYS] ?: "1,2,3,4,5"
            val currentSet = currentString.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableSet()

            if (currentSet.contains(dayOfWeek)) {
                if (currentSet.size > 1) {
                    currentSet.remove(dayOfWeek)
                }
            } else {
                currentSet.add(dayOfWeek)
            }
            prefs[PreferenceKeys.SCHEDULE_DAYS] = currentSet.sorted().joinToString(",")
        }
    }

    /**
     * Records that an intentional app session was started (e.g. user opened Instagram/YouTube).
     */
    suspend fun recordSessionStart() {
        context.dataStore.edit { prefs ->
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val lastResetDate = prefs[PreferenceKeys.LAST_COUNTER_RESET_DATE] ?: ""
            val isNewDay = lastResetDate != todayStr

            val newSessions = if (isNewDay) 1 else ((prefs[PreferenceKeys.SESSIONS_TODAY] ?: 0) + 1)
            val newClean = if (isNewDay) 1 else ((prefs[PreferenceKeys.CLEAN_SESSIONS_TODAY] ?: 0) + 1)
            val currentIntercepts = if (isNewDay) 0 else (prefs[PreferenceKeys.BLOCKED_COUNT_TODAY] ?: 0)

            prefs[PreferenceKeys.SESSIONS_TODAY] = newSessions
            prefs[PreferenceKeys.CLEAN_SESSIONS_TODAY] = newClean
            prefs[PreferenceKeys.LAST_COUNTER_RESET_DATE] = todayStr
            if (isNewDay) {
                prefs[PreferenceKeys.BLOCKED_COUNT_TODAY] = 0
            }

            saveDayToHistory(prefs, todayStr, newSessions, newClean, currentIntercepts)
        }
    }

    /**
     * Increments the blocked attempts counter and marks current visit as an intercepted session.
     */
    suspend fun incrementBlockedCount() {
        context.dataStore.edit { prefs ->
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val lastResetDate = prefs[PreferenceKeys.LAST_COUNTER_RESET_DATE] ?: ""
            val isNewDay = lastResetDate != todayStr

            val newIntercepts = if (isNewDay) 1 else ((prefs[PreferenceKeys.BLOCKED_COUNT_TODAY] ?: 0) + 1)
            val totalSessions = if (isNewDay) 1 else (prefs[PreferenceKeys.SESSIONS_TODAY] ?: 1).coerceAtLeast(1)
            // Adjust clean sessions downward so this visit is recorded as intercepted
            val currentClean = if (isNewDay) 0 else (prefs[PreferenceKeys.CLEAN_SESSIONS_TODAY] ?: 0)
            val adjustedClean = (currentClean - 1).coerceAtLeast(0)

            prefs[PreferenceKeys.BLOCKED_COUNT_TODAY] = newIntercepts
            prefs[PreferenceKeys.CLEAN_SESSIONS_TODAY] = adjustedClean
            prefs[PreferenceKeys.SESSIONS_TODAY] = totalSessions
            prefs[PreferenceKeys.LAST_COUNTER_RESET_DATE] = todayStr

            saveDayToHistory(prefs, todayStr, totalSessions, adjustedClean, newIntercepts)
            val currentHour = java.time.LocalDateTime.now().hour
            saveHourlyDistribution(prefs, currentHour)
        }
    }

    private fun saveHourlyDistribution(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        hour: Int
    ) {
        val currentJson = prefs[PreferenceKeys.HOURLY_DISTRIBUTION_JSON] ?: "{}"
        val hourlyObj = try { JSONObject(currentJson) } catch (_: Exception) { JSONObject() }
        val hourKey = hour.toString()
        val currentCount = hourlyObj.optInt(hourKey, 0)
        hourlyObj.put(hourKey, currentCount + 1)
        prefs[PreferenceKeys.HOURLY_DISTRIBUTION_JSON] = hourlyObj.toString()
    }

    private fun saveDayToHistory(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        todayStr: String,
        sessions: Int,
        clean: Int,
        intercepts: Int
    ) {
        val currentJson = prefs[PreferenceKeys.WEEKLY_HISTORY_JSON] ?: "{}"
        val historyObj = try { JSONObject(currentJson) } catch (_: Exception) { JSONObject() }

        val dayObj = JSONObject().apply {
            put("sessions", sessions)
            put("clean", clean)
            put("intercepts", intercepts)
        }
        historyObj.put(todayStr, dayObj)

        // Prune entries older than 14 days
        val cutoff = LocalDate.now().minusDays(14)
        val keys = historyObj.keys()
        val toRemove = mutableListOf<String>()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                val entryDate = LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE)
                if (entryDate.isBefore(cutoff)) toRemove.add(key)
            } catch (_: Exception) {
                toRemove.add(key)
            }
        }
        toRemove.forEach { historyObj.remove(it) }

        prefs[PreferenceKeys.WEEKLY_HISTORY_JSON] = historyObj.toString()
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETE] = true
        }
    }

    suspend fun setDisclosureAccepted() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DISCLOSURE_ACCEPTED] = true
        }
    }
}
