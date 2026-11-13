# ReelBlocker — Project Status & Next Steps

> **Project:** Instagram Reels & YouTube Shorts Blocker for Android  
> **Package:** `com.reelblocker.app`  
> **Architecture:** 100% Native Kotlin · Jetpack Compose (M3) · AccessibilityService API · Jetpack DataStore · Firebase Remote Config  
> **Status Date:** November 2026

---

## 1. Executive Summary & Codebase Review

All code development for **Weeks 1, 2, and 3** plus core engine optimizations from **Week 4** is **100% implemented** and ready in the repository.

### Summary Progress:
- **Total tasks planned:** ~90 subtasks
- **Completed in code:** 68 subtasks (All core engine, UI, preferences, remote config, edge case handlers, and unit tests)
- **Pending verification / manual steps:** 22 subtasks (Physical device tests, Google Services JSON file placement, Play Console listing & demo video)

---

## 2. Component-by-Component Implementation Status

### ⚙️ Core Engine (`service/`)
- **`BlockerAccessibilityService.kt`**:
  - Monitors `com.instagram.android` and `com.google.android.youtube` via `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED`.
  - **Debounce Cooldown**: Default 800ms cooldown (dynamically adjustable via Remote Config) prevents back-press spam.
  - **Memory Management**: Uses `safeRecycle()` for `AccessibilityNodeInfo` on API < 33 to prevent OS memory leaks.
  - **Zero Background I/O**: Preferences are cached in memory via coroutine `StateFlow`; counter increments run on `SupervisorJob() + Dispatchers.Main.immediate`.

### 🔀 Edge Case Handling (`service/`)
- **Instagram Dedicated Reels Tab**: If the bottom navigation Reels tab is selected, programmatically clicks the `Home` / `Feed` tab node via `ACTION_CLICK` to return to the feed without looping or exiting the app.
- **Instagram DM Reels**: If opened from a direct message or feed post, performs `GLOBAL_ACTION_BACK` to return to the previous screen/chat.
- **YouTube Shorts Carousel vs. Player**: Measures player container height (`bounds.height() > 600px`) and targets container IDs to distinguish the full-screen player from horizontal suggestion carousels on the Home feed.
- **YouTube Shorts Bottom Tab**: If the user taps the dedicated Shorts tab, programmatically redirects to the YouTube Home tab.
- **Remote Kill Switch**: When `kill_switch: true` is activated in Firebase, the service skips all detection and blocking immediately.

### 📱 User Interface (`ui/` — 3-Page Architecture with WhatsApp-Style Bottom Navigation)
- **`MainScreen.kt` (Bottom Navigation Hub)**:
  - Material 3 `NavigationBar` with WhatsApp-style tab switching:
    - **Home**: Live Focus status, Hero card, Snooze breather pass, protected apps toggles, micro-haptics.
    - **Schedule**: Dedicated Focus Scheduler with interactive custom time steppers and presets.
    - **Dashboard**: Multi-graph analytics, 7-day clean rate chart, hourly vulnerability heatmap, and habit score.
  - Reactive badges: Error badge for missing accessibility permissions; active badge during live focus windows.
- **`HomeScreen.kt` (Day-to-day Operations)**:
  - Hero Focus Card with pulsing accent state (Active / Scheduled / Paused / Inactive).
  - Quick Snooze ("Take a 5-min breather") with live countdown and one-tap resume.
  - Today's Habit Impact snapshot: Clean rate %, Deflected urges, Time reclaimed (~XXm), and Zero-Urge Mastery badge.
  - Protected apps: Instagram (Reels & Explore) with DM/profile safety guarantees, and YouTube Shorts.
  - Haptic bump on block switch.
- **`ScheduleScreen.kt` (Dedicated Focus Scheduler)**:
  - In-place interactive digital time editor:
    - Hour steppers (`-` / `+`) with 12-hour AM/PM formatting.
    - Minute steppers with 15-minute intervals and quick chips (`:00`, `:15`, `:30`, `:45`).
    - AM/PM segmented toggle.
    - Live daily duration calculator (e.g. `8.0 hrs/day`).
  - One-tap quick presets: *Workday (9 AM - 5 PM)*, *Deep Work (10 AM - 1 PM)*, *Night Guard (10 PM - 7 AM)*, *All Day Shield (8 AM - 10 PM)*.
  - Interactive weekday selector chips (`M`, `T`, `W`, `T`, `F`, `S`, `S`) with quick *Mon-Fri* and *Everyday* filters.
  - Behavioral science insight card on eliminating decision fatigue.
- **`AnalyticsScreen.kt` (Habit & Attention Dashboard)**:
  - Focus Mastery Rank Hero Card ("Zen Master 🧘", "Focus Warrior 🛡️", etc.) with 3 KPI badges.
  - **Graph 1**: 7-Day Clean Session Rate Bar Chart with Zero-Urge Mastery `⭐` badges.
  - **Graph 2**: Hourly Doomscroll Vulnerability Clock (Morning, Afternoon, Evening, Night) with urge heatmaps and focus schedule recommendation.
  - **Graph 3**: Surface Temptation Share comparing Instagram (Reels & Explore) vs YouTube Shorts.
  - **Metric 4**: Habit Reconditioning Model explaining the clean session framework.
- **`ProminentDisclosureDialog.kt`**:
  - **Play Store Mandatory**: Explicitly notifies users that accessibility access is strictly local to Instagram/YouTube, zero personal data is gathered, and no data leaves the device.
- **`OnboardingScreen.kt`**:
  - 3-step animated pager explaining features, privacy, and guiding users through enabling the service.
- **`AppNavigation.kt` & `MainActivity.kt`**:
  - Reactive routing based on `onboardingComplete` DataStore flag.

### 💾 Data & Configuration Layer (`data/`)
- **`UserPreferencesRepository.kt`**:
  - Built with Jetpack DataStore (Preferences).
  - Handles auto-reset of the daily counter by comparing `LAST_COUNTER_RESET_DATE` with `LocalDate.now()`.
- **`ViewIdProvider.kt`**:
  - Reads View IDs from Firebase Remote Config with fallback to hardcoded defaults.
  - Unit-tested via `ViewIdProviderTest.kt`.
- **`RemoteConfigRepository.kt`**:
  - Configured with `0s` fetch interval in Debug and `12h` in Release.

### 🛡️ Build & Security Configuration
- **`build.gradle.kts`**: Compose BOM 2024.12.01, Kotlin 2.1.0, AGP 8.7.3, Firebase BOM 33.7.0, DataStore 1.1.1, JUnit 4, and `org.json:json`.
- **`proguard-rules.pro`**: Keep rules added for `BlockerAccessibilityService`, Firebase Remote Config, and model data classes.
- **`accessibility_service_config.xml`**: Strict package filter (`com.instagram.android,com.google.android.youtube`) ensuring Google Play approval compliance.

---

## 3. Project File Tree

```
app_avoid_poc/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── TASKS.md
├── CURRENT_STATUS_AND_NEXT_STEPS.md
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── test/java/com/reelblocker/app/data/
        │   └── ViewIdProviderTest.kt
        └── main/
            ├── AndroidManifest.xml
            ├── res/
            │   ├── xml/accessibility_service_config.xml
            │   ├── xml/remote_config_defaults.xml
            │   ├── values/strings.xml
            │   ├── values/colors.xml
            │   ├── values/themes.xml
            │   └── mipmap-hdpi/ic_launcher.xml
            └── java/com/reelblocker/app/
                ├── ReelBlockerApp.kt
                ├── MainActivity.kt
                ├── model/
                │   ├── BlockerTarget.kt
                │   └── UserPreferences.kt
                ├── data/
                │   ├── UserPreferencesRepository.kt
                │   ├── ViewIdProvider.kt
                │   └── RemoteConfigRepository.kt
                ├── service/
                │   └── BlockerAccessibilityService.kt
                ├── ui/
                │   ├── theme/Theme.kt
                │   ├── components/ProminentDisclosureDialog.kt
                │   ├── main/
                │   │   └── MainScreen.kt
                │   ├── home/
                │   │   └── HomeScreen.kt
                │   ├── schedule/
                │   │   └── ScheduleScreen.kt
                │   ├── analytics/
                │   │   └── AnalyticsScreen.kt
                │   ├── dashboard/
                │   │   ├── DashboardScreen.kt
                │   │   └── DashboardViewModel.kt
                │   ├── onboarding/
                │   │   ├── OnboardingScreen.kt
                │   │   └── OnboardingViewModel.kt
                │   └── navigation/AppNavigation.kt
                └── util/
                    ├── Constants.kt
                    ├── AccessibilityPermissionChecker.kt
                    ├── BatteryOptimizationHelper.kt
                    └── DebugNodeLogger.kt
```

---

## 4. Immediate Next Needed Steps

The remaining work consists of physical device testing, external service setup, and Play Store preparation.

### Step 1: Place `google-services.json` (COMPLETED ✅)
- Firebase project registered: `reel-blocker-9cf74`
- Package name matched: `com.reelblocker.app`
- File placed at: `app_avoid_poc/app/google-services.json`

### Step 2: Test on a Physical Device & Verify View IDs
- Connect an Android device (via USB or WiFi ADB).
- Run the app via Android Studio (`Run 'app'`).
- Open Instagram and YouTube on the physical device.
- Use `DebugNodeLogger` (already wired in `app/src/main/java/com/reelblocker/app/util/DebugNodeLogger.kt`) to check if Instagram or YouTube updated their View IDs in the latest app versions:
  ```kotlin
  DebugNodeLogger.dumpNodeTree(rootInActiveWindow, "com.instagram.android")
  ```
- If an ID changed, update Firebase Remote Config or `ViewIdProvider.kt`.

### Step 3: Verify Edge Cases on Device
- [ ] Open a Reel sent in an Instagram Direct Message (verify it returns to the chat).
- [ ] Tap the Instagram bottom Reels icon (verify it switches to Home feed).
- [ ] Scroll past YouTube Shorts carousel on the Home feed (verify it does **not** block).
- [ ] Open full-screen YouTube Short (verify it blocks and returns).
- [ ] Tap the YouTube bottom Shorts tab (verify it redirects to Home).

### Step 4: Record Demo Video for Google Play Submission
Google requires a demonstration video for any app using the `AccessibilityService` API:
1. Show the **Prominent Disclosure Dialog** upon first launch.
2. Show the user accepting and granting the permission in Android Settings.
3. Open YouTube and Instagram to demonstrate short-form blocking in real time.
4. Emphasize that no personal data is collected or shared (matches the Data Safety form).

### Step 5: Build Release APK / AAB
- Generate your release signing key.
- Build release bundle:
  ```bash
  ./gradlew bundleRelease
  ```
- Upload to Google Play Console (Closed Testing / Internal Track first).
