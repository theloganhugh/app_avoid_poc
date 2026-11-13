package com.reelblocker.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelblocker.app.data.UserPreferencesRepository
import com.reelblocker.app.data.ViewIdProvider
import com.reelblocker.app.model.UserPreferences
import com.reelblocker.app.util.AccessibilityPermissionChecker
import com.reelblocker.app.util.BatteryOptimizationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 * Manages user preferences state, system permissions, and live snooze countdown.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    private val viewIdProvider = ViewIdProvider()

    /** User preferences as observable state. */
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    /** Whether the Accessibility Service is currently enabled. */
    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    /** Whether battery optimization is active for this app. */
    private val _isBatteryOptimized = MutableStateFlow(true)
    val isBatteryOptimized: StateFlow<Boolean> = _isBatteryOptimized.asStateFlow()

    /** Whether the remote kill switch is active. */
    private val _isKillSwitchActive = MutableStateFlow(false)
    val isKillSwitchActive: StateFlow<Boolean> = _isKillSwitchActive.asStateFlow()

    /** Whether to show the prominent disclosure dialog. */
    private val _showDisclosureDialog = MutableStateFlow(false)
    val showDisclosureDialog: StateFlow<Boolean> = _showDisclosureDialog.asStateFlow()

    /** Remaining snooze seconds (ticking countdown for UI). */
    private val _snoozeRemainingSeconds = MutableStateFlow(0L)
    val snoozeRemainingSeconds: StateFlow<Long> = _snoozeRemainingSeconds.asStateFlow()

    init {
        // Ticking countdown loop when snooze is active
        viewModelScope.launch {
            preferences.collectLatest { prefs ->
                while (prefs.isSnoozed) {
                    val remainingMs = prefs.snoozeUntilMs - System.currentTimeMillis()
                    if (remainingMs <= 0) {
                        _snoozeRemainingSeconds.value = 0L
                        repository.cancelSnooze()
                        break
                    }
                    _snoozeRemainingSeconds.value = (remainingMs / 1000L).coerceAtLeast(0L)
                    delay(1000L)
                }
                if (!prefs.isSnoozed) {
                    _snoozeRemainingSeconds.value = 0L
                }
            }
        }
    }

    /** Refresh system permission statuses (call from onResume). */
    fun refreshSystemStatus() {
        val context = getApplication<Application>()
        _isAccessibilityEnabled.value = AccessibilityPermissionChecker.isServiceEnabled(context)
        _isBatteryOptimized.value = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        _isKillSwitchActive.value = try { viewIdProvider.isKillSwitchActive() } catch (_: Exception) { false }
    }

    fun setMasterToggle(enabled: Boolean) {
        viewModelScope.launch { repository.setMasterToggle(enabled) }
    }

    fun setBlockInstagramReels(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockInstagramReels(enabled) }
    }

    fun setBlockInstagramExplore(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockInstagramExplore(enabled) }
    }

    fun setBlockYouTubeShorts(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockYouTubeShorts(enabled) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticFeedbackEnabled(enabled) }
    }

    /** Snooze protection for given minutes (e.g. 5 or 15 mins). */
    fun snooze(minutes: Int) {
        viewModelScope.launch { repository.snooze(minutes) }
    }

    /** Cancel snooze and immediately re-arm protection. */
    fun cancelSnooze() {
        viewModelScope.launch { repository.cancelSnooze() }
    }

    fun setScheduleEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setScheduleEnabled(enabled) }
    }

    fun setScheduleTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch { repository.setScheduleTime(startHour, startMinute, endHour, endMinute) }
    }

    fun toggleScheduleDay(dayOfWeek: Int) {
        viewModelScope.launch { repository.toggleScheduleDay(dayOfWeek) }
    }

    fun requestEnableAccessibility() {
        _showDisclosureDialog.value = true
    }

    fun dismissDisclosureDialog() {
        _showDisclosureDialog.value = false
    }

    fun acceptDisclosure() {
        viewModelScope.launch {
            repository.setDisclosureAccepted()
            _showDisclosureDialog.value = false
        }
    }
}
