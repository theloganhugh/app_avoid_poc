# Short-Form Content Blocker — Implementation Tasks

> **Project:** Instagram Reels & YouTube Shorts Blocker (Android)
> **Stack:** Kotlin · Jetpack Compose (M3) · AccessibilityService · DataStore · Firebase Remote Config
> **Timeline:** 4-Week Sprint

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ⬜ | Not started |
| 🔵 | In progress |
| ✅ | Done |
| 🔴 | Blocked |

---

## Week 1 — Core Engine & Node Inspection

### Task 1.1: Project Setup
- ✅ **1.1.1** Create new Android Studio project (`com.reelblocker.app`) with Kotlin DSL Gradle, `minSdk 26`, `targetSdk 35`.
- ✅ **1.1.2** Add Jetpack Compose BOM, Material 3, and DataStore dependencies in `build.gradle.kts`.
- ✅ **1.1.3** Add Firebase BOM and `firebase-config-ktx` dependency.
- ✅ **1.1.4** Configure `google-services.json` (create Firebase project, register the Android app).
- ✅ **1.1.5** Set up base package structure:
  ```
  com.reelblocker.app/
  ├── service/          # AccessibilityService
  ├── ui/theme/         # Compose theme
  ├── data/             # DataStore, Remote Config repo
  ├── model/            # Data classes
  └── util/             # Helpers, constants
  ```

### Task 1.2: AndroidManifest Configuration
- ✅ **1.2.1** Declare permissions: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `POST_NOTIFICATIONS`.
- ✅ **1.2.2** Register `MainActivity` with launcher intent filter.
- ✅ **1.2.3** Register `BlockerAccessibilityService` with `BIND_ACCESSIBILITY_SERVICE` permission and intent filter.
- ✅ **1.2.4** Link `@xml/accessibility_service_config` via `<meta-data>`.

### Task 1.3: Accessibility Service Config XML
- ✅ **1.3.1** Create `res/xml/accessibility_service_config.xml`.
- ✅ **1.3.2** Restrict `android:packageNames` to `com.instagram.android,com.google.android.youtube`.
- ✅ **1.3.3** Set event types: `typeWindowStateChanged | typeWindowContentChanged`.
- ✅ **1.3.4** Set flags: `flagReportViewIds | flagIncludeNotImportantViews`.
- ✅ **1.3.5** Set `canRetrieveWindowContent="true"`, `notificationTimeout="150"`.
- ✅ **1.3.6** Add `@string/accessibility_service_description` in `strings.xml` (clear, user-friendly text required for Play Store).

### Task 1.4: Core Blocker Service Skeleton
- ✅ **1.4.1** Create `BlockerAccessibilityService.kt` extending `AccessibilityService`.
- ✅ **1.4.2** Implement `onAccessibilityEvent()` with package-name routing (`com.google.android.youtube`, `com.instagram.android`).
- ✅ **1.4.3** Implement debounce/cooldown mechanism (configurable via Remote Config, default 800ms) to prevent rapid repeated back presses.
- ✅ **1.4.4** Implement `triggerBlockAction()` using `performGlobalAction(GLOBAL_ACTION_BACK)`.
- ✅ **1.4.5** Implement `onInterrupt()` with logging.

### Task 1.5: YouTube Shorts Detection Logic
- ✅ **1.5.1** Implement `isYouTubeShortsActive(rootNode)` method.
- ✅ **1.5.2** Check against known View IDs (via `ViewIdProvider`):
  - `com.google.android.youtube:id/reel_recycler`
  - `com.google.android.youtube:id/reel_player_page_container`
  - `com.google.android.youtube:id/shorts_player_container`
- ✅ **1.5.3** Validate nodes are `isVisibleToUser` before triggering block.
- ⬜ **1.5.4** **Manual verification:** Use Layout Inspector on a physical device running the latest YouTube to confirm/update View IDs.

### Task 1.6: Instagram Reels Detection Logic
- ✅ **1.6.1** Implement `isInstagramReelsActive(rootNode)` method.
- ✅ **1.6.2** Check against known View IDs (via `ViewIdProvider`):
  - `com.instagram.android:id/clips_viewer_view_pager`
  - `com.instagram.android:id/reel_viewer_root`
- ✅ **1.6.3** Validate nodes are `isVisibleToUser` before triggering block.
- ⬜ **1.6.4** **Manual verification:** Use Layout Inspector / debug logger on physical device with latest Instagram to confirm/update View IDs.

### Task 1.7: Debug Logger Utility (Development Only)
- ✅ **1.7.1** Build a debug utility that dumps the full `AccessibilityNodeInfo` tree to Logcat when a target app is in the foreground.
- ✅ **1.7.2** Log: View ID, class name, content description, text, bounds, visibility, clickability.
- ✅ **1.7.3** Gate behind `BuildConfig.DEBUG` so it never ships in release builds.

---

## Week 2 — User Interface & Onboarding

### Task 2.1: DataStore Setup (Preferences)
- ✅ **2.1.1** Create `UserPreferencesRepository` using Jetpack DataStore (Preferences).
- ✅ **2.1.2** Define preference keys:
  - `MASTER_TOGGLE` (Boolean) — global ON/OFF.
  - `BLOCK_INSTAGRAM_REELS` (Boolean) — per-app toggle.
  - `BLOCK_YOUTUBE_SHORTS` (Boolean) — per-app toggle.
  - `BLOCKED_COUNT_TODAY` (Int) — daily counter.
  - `LAST_COUNTER_RESET_DATE` (String) — ISO date for resetting counter at midnight.
- ✅ **2.1.3** Expose preferences as `Flow<UserPreferences>` data class.
- ✅ **2.1.4** Wire `BlockerAccessibilityService` to read toggle states from DataStore before executing block logic.

### Task 2.2: Dashboard Screen (Main UI)
- ✅ **2.2.1** Create `DashboardScreen` composable with Scaffold + TopAppBar.
- ✅ **2.2.2** Build **Master Toggle** card — large switch with animated ON/OFF state (green/red).
- ✅ **2.2.3** Build **Per-App Toggle** section:
  - Instagram Reels toggle with Instagram icon.
  - YouTube Shorts toggle with YouTube icon.
- ✅ **2.2.4** Build **Statistics Card** — "Blocked Today: X" with daily auto-reset at midnight.
- ✅ **2.2.5** Build **Service Status Indicator** — show whether the Accessibility Service is currently enabled (green dot) or disabled (red dot with "Tap to enable" action).
- ✅ **2.2.6** Add **Battery Optimization Status** row — warn if battery optimization is not disabled for the app.

### Task 2.3: Prominent Disclosure Dialog
> [!IMPORTANT]
> This dialog is **mandatory** for Google Play Store approval when using the Accessibility API.

- ✅ **2.3.1** Create `ProminentDisclosureDialog` composable.
- ✅ **2.3.2** Content must clearly state:
  - What data is accessed (screen content of Instagram & YouTube only).
  - Why it's accessed (to detect and exit Reels/Shorts player screens).
  - That **zero** personal data leaves the device.
- ✅ **2.3.3** Include "I Understand & Agree" and "Cancel" buttons.
- ✅ **2.3.4** Store consent state in DataStore; show dialog only once (or on major updates).
- ✅ **2.3.5** Only redirect to Accessibility Settings **after** user consents.

### Task 2.4: Accessibility Permission Flow
- ✅ **2.4.1** Create `AccessibilityPermissionChecker` utility:
  - Check if `BlockerAccessibilityService` is currently enabled via `AccessibilityManager`.
- ✅ **2.4.2** On Dashboard, show current status and "Enable" button.
- ✅ **2.4.3** On "Enable" tap → show Prominent Disclosure → on consent → launch `Settings.ACTION_ACCESSIBILITY_SETTINGS` intent.
- ✅ **2.4.4** On return to app, re-check status and update UI.

### Task 2.5: Onboarding Flow (First Launch)
- ✅ **2.5.1** Create a 3-step onboarding pager:
  - **Step 1:** "What This App Does" — explanation with illustration.
  - **Step 2:** "Privacy First" — prominent disclosure content.
  - **Step 3:** "Enable the Service" — guide user to Accessibility Settings.
- ✅ **2.5.2** Track `ONBOARDING_COMPLETE` in DataStore; skip on subsequent launches.
- ✅ **2.5.3** Wire `MainActivity` to route to Onboarding vs. Dashboard based on flag.

---

## Week 3 — Remote Config & Edge Cases

### Task 3.1: Firebase Remote Config Integration
- ✅ **3.1.1** Initialize Firebase in `Application` class.
- ✅ **3.1.2** Create `RemoteConfigRepository`:
  - Fetch and activate Remote Config on app start.
  - Set minimum fetch interval (12 hours for production, 0 for debug).
- ✅ **3.1.3** Define Remote Config keys:
  - `youtube_shorts_view_ids` — JSON array of View ID strings.
  - `instagram_reels_view_ids` — JSON array of View ID strings.
  - `block_cooldown_ms` — Long (tunable cooldown).
  - `kill_switch` — Boolean (remotely disable all blocking in emergencies).
- ✅ **3.1.4** Set in-app defaults in `res/xml/remote_config_defaults.xml` matching hardcoded fallback values.
- ✅ **3.1.5** Refactor `BlockerAccessibilityService` to read View IDs from Remote Config instead of hardcoded lists.

### Task 3.2: Dynamic View ID Loader
- ✅ **3.2.1** Create `ViewIdProvider` class that:
  - Reads cached Remote Config values.
  - Parses JSON arrays into `List<String>`.
  - Falls back to hardcoded defaults if Remote Config is unavailable.
- ✅ **3.2.2** Inject `ViewIdProvider` into `BlockerAccessibilityService`.
- ✅ **3.2.3** Add unit tests for JSON parsing and fallback behavior.

### Task 3.3: Edge Case — Reel from Instagram DMs
- ⬜ **3.3.1** Test scenario: User receives a Reel link in a DM → taps it → Reel opens in full-screen player.
- ⬜ **3.3.2** Verify that `GLOBAL_ACTION_BACK` returns the user to the DM thread (not to the Home feed or outside the app).
- ✅ **3.3.3** Implemented non-destructive navigation: when not on bottom Reels tab, `GLOBAL_ACTION_BACK` returns cleanly to previous screen/thread.

### Task 3.4: Edge Case — Instagram Reels Tab (Bottom Nav)
- ⬜ **3.4.1** Test scenario: User taps the dedicated "Reels" icon on Instagram's bottom navigation bar.
- ⬜ **3.4.2** Verify that `GLOBAL_ACTION_BACK` exits the Reels tab.
- ✅ **3.4.3** Implemented bottom-tab redirect: detects if Reels tab is selected and programmatically triggers `ACTION_CLICK` on the Home feed tab node.
- ✅ **3.4.4** Graceful fallback to `GLOBAL_ACTION_BACK` if the Home tab node is not interactable.

### Task 3.5: Edge Case — YouTube Shorts Tab & Suggestions
- ⬜ **3.5.1** Test scenario: User taps "Shorts" tab on YouTube's bottom navigation.
- ⬜ **3.5.2** Test scenario: User scrolls past a Shorts suggestion card in the Home feed and it auto-expands.
- ✅ **3.5.3** Differentiated between full-screen Shorts player (height threshold) and compact Home feed suggestion carousels so standard YouTube browsing is not blocked.
- ✅ **3.5.4** Implemented bottom-tab redirection to YouTube Home tab when the dedicated Shorts tab is opened.

### Task 3.6: Kill Switch & Remote Disable
- ✅ **3.6.1** Read `kill_switch` boolean from Remote Config in `onAccessibilityEvent()`.
- ✅ **3.6.2** If `true`, skip all detection and blocking (silent no-op).
- ✅ **3.6.3** Show subtle "Blocking is temporarily paused remotely while updates are being deployed" banner on Dashboard.

---

## Week 4 — Optimization, Polish & Release Prep

### Task 4.1: Battery Optimization Bypass
- ✅ **4.1.1** Check if app is exempt from battery optimization via `PowerManager.isIgnoringBatteryOptimizations()`.
- ✅ **4.1.2** Show battery card on Dashboard prompting user with dynamic status.
- ✅ **4.1.3** Launch `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` intent on user tap.
- ✅ **4.1.4** Provide OEM-specific guidance text for:
  - **Samsung OneUI:** Settings → Battery → Background usage limits → Never sleeping apps.
  - **Xiaomi MIUI/HyperOS:** Settings → Apps → Manage apps → [App] → Battery saver → No restrictions.
  - **OnePlus OxygenOS:** Settings → Battery → Battery optimization → [App] → Don't optimize.
  - **Huawei / Vivo / OPPO / Realme:** Tailored step-by-step instructions.
- ✅ **4.1.5** Detect manufacturer via `Build.MANUFACTURER` and show relevant instructions dynamically.

### Task 4.2: Memory & Performance Optimization
- ✅ **4.2.1** Ensure all `AccessibilityNodeInfo` objects are properly recycled via `safeRecycle()` on API < 33 to prevent memory leaks.
- ⬜ **4.2.2** Profile service memory usage with Android Studio Profiler on device; target < 5 MB resident.
- ✅ **4.2.3** Ensure `onAccessibilityEvent()` completes in < 50ms (zero disk/network I/O on callback thread; in-memory preferences & async counter updates).
- ✅ **4.2.4** Minimize node tree traversal depth — exit early on first match.

### Task 4.3: Blocked Counter Persistence & Analytics
- ✅ **4.3.1** Increment `BLOCKED_COUNT_TODAY` in DataStore each time `triggerBlockAction()` fires.
- ✅ **4.3.2** Reset counter at midnight (compare `LAST_COUNTER_RESET_DATE` to current date).
- ✅ **4.3.3** Expose counter as a reactive `StateFlow` to the Dashboard for live updates.
- ✅ **4.3.4** **Weekly 7-Day Time-Saved Graph & Analytics**: Persistent 14-day rolling history in DataStore with interactive 7-day bar chart showing daily intercepted blocks and estimated time reclaimed.
- ✅ **4.3.5** **Scheduled Focus Hours**: Automated Workday / Study blocker (e.g. 9 AM – 5 PM) with custom intervals, quick presets, weekday chip selectors, and automatic protection enforcement.

### Task 4.4: Notifications
- ⬜ **4.4.1** Create a notification channel (`blocker_service`) for the persistent foreground-service notification (if needed for service survival on aggressive OEMs).
- ⬜ **4.4.2** Show a subtle persistent notification: "Content Blocker is active" (required by some OEMs to prevent killing).
- ⬜ **4.4.3** Optionally show a brief notification each time a block is triggered ("Blocked a YouTube Short").
- ⬜ **4.4.4** Make block notifications configurable (ON/OFF toggle in settings).

### Task 4.5: Testing & QA
- ✅ **4.5.1** Unit tests:
  - `ViewIdProviderTest` covering JSON parsing, fallback lists, defaults, and kill switch.
- ⬜ **4.5.2** Integration tests on device / emulator:
  - Verify AccessibilityService registers and receives events.
  - Verify Remote Config fetch and fallback.
- ⬜ **4.5.3** Manual test matrix (physical devices):

  | Device | OS Version | Instagram Version | YouTube Version | Result |
  |--------|-----------|-------------------|-----------------|--------|
  | Pixel 7 | Android 14 | Latest | Latest | ⬜ |
  | Samsung S23 | Android 14 (OneUI 6) | Latest | Latest | ⬜ |
  | Xiaomi 13 | Android 14 (HyperOS) | Latest | Latest | ⬜ |
  | OnePlus 11 | Android 14 (OxygenOS 14) | Latest | Latest | ⬜ |
  | Older device | Android 10 | Latest | Latest | ⬜ |

### Task 4.6: Play Store Submission Prep
- ⬜ **4.6.1** Write a clear Accessibility Service declaration for the Play Console (required form).
- ⬜ **4.6.2** Record a **demonstration video** (< 2 min) showing:
  - The prominent disclosure dialog.
  - The service being enabled.
  - A Reel/Short being blocked in action.
  - Proof that no data is collected or transmitted.
- ⬜ **4.6.3** Prepare store listing assets: icon, feature graphic, screenshots, description.
- ⬜ **4.6.4** Fill out the Data Safety section (no data collected, no data shared).
- ⬜ **4.6.5** Submit for review with the Accessibility permission declaration + video.

### Task 4.7: ProGuard / R8 Configuration
- ✅ **4.7.1** Add ProGuard keep rules for `BlockerAccessibilityService` in `proguard-rules.pro`.
- ✅ **4.7.2** Add keep rules for Firebase Remote Config classes in `proguard-rules.pro`.
- ✅ **4.7.3** Add keep rules for DataStore and Model classes.
- ⬜ **4.7.4** Run release build verification (`./gradlew assembleRelease`).

---

## Summary

| Week | Focus | Tasks |
|------|-------|-------|
| **1** | Core Engine & Node Inspection | 1.1 – 1.7 (23 subtasks) |
| **2** | UI & Onboarding | 2.1 – 2.5 (22 subtasks) |
| **3** | Remote Config & Edge Cases | 3.1 – 3.6 (18 subtasks) |
| **4** | Optimization & Release | 4.1 – 4.7 (27 subtasks) |
| | **Total** | **~90 subtasks** |

> [!TIP]
> Use Firebase Remote Config early (Week 3) so you can push View ID updates the moment Instagram or YouTube ships a UI change — no app update needed.

> [!WARNING]
> Always test on **physical devices**. Accessibility Services behave differently on emulators, and OEM battery optimizations are not replicated in AVDs.
