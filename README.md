# Daymark — Your day, clearly marked.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34-blue.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Production--Ready-brightgreen.svg)]()

> **Daymark** is a minimal personal productivity application designed for deep focus and clarity. Offline-first, local-only, zero accounts, zero advertisements, and zero subscriptions.

---

## ✦ Product Philosophy

Daymark is intentionally restrained. In a landscape saturated with bloated project management software, AI assistants, social feeds, and gamified dopamine loops, Daymark returns to first principles:

1. **Two Primary Tabs**: Exactly **Todo** and **Goals**. No extra tabs for motivation or analytics.
2. **ChatGPT-Inspired Segmented Switch**: A smooth sliding active pill indicator with 280ms easing transitions.
3. **Settings Beside the Switch**: Settings sheet opens directly beside the pill switch in the stationary top bar.
4. **Max 6 Active Goals**: Enforcing focus. Doing 3 to 6 things consistently delivers compounding momentum.
5. **60:30:10 Design Tokens**: Harmonic, calm palettes with 60% surface background, 30% container surface, and 10% accent color.
6. **Task Timers & Focus Mode**: Focus sessions integrated with tasks or standalone, supported by an Android Foreground Service and real-time synthesized acoustic ambient noise.
7. **Daily Motivation & Personal Video**: Built-in rotating quotes and personal video motivation via Media3 ExoPlayer.
8. **Interactive Home-Screen Widgets**: Four redesigned Jetpack Glance widgets with direct two-way interactivity (`ActionCallback`).
9. **100% Offline & Private**: Full JSON export and import for seamless personal data ownership.

---

## ✦ Key Features & Product Updates

### 1. Navigation & Segmented Switching
- **Stationary Top Header**: Daymark branding with custom tick icon, pill switcher, and settings gear.
- **ChatGPT-Inspired Pill Switcher**: Connected pill options with an animated sliding background.
- **Slide + Subtle Fade Transitions**: 280ms duration using `FastOutSlowInEasing`.
- **Accessibility**: Support for reduced motion, TalkBack content descriptions, touch targets $\ge 48\text{dp}$, and accessible contextual actions.

### 2. Daily Motivation
- **Daily Rotating Quotes**: Curated offline repository of 30+ philosophical and productivity quotes (Marcus Aurelius, Bruce Lee, James Clear, etc.).
- **Time-of-Day Greetings**: Contextual greetings for morning, afternoon, evening, and night.
- **Mindful Reflection Prompts**: Daily prompts encouraging clarity, intention, and gentle reflection.
- **Encouraging Completion Messages**: Non-intrusive completion encouragements (*Clearly marked*, *Quiet progress*, *Momentum built*).
- **Settings Toggle**: Enable or disable daily motivation features from Settings.

### 3. Personal Video Motivation (Media3 ExoPlayer)
- **Local Gallery Selection**: Pick personal motivation videos directly via Android PhotoPicker / SAF (`OpenDocument`) with persistable URI permissions.
- **Prominent Play Button**: One-tap playback of your chosen video when you need encouragement.
- **Native ExoPlayer Playback**: Fullscreen toggle, volume mute, seekbar, play/pause controls.
- **Playback Mode Setting**: Choose between starting from the beginning (0:00) every time or resuming from the last position.
- **Graceful Error Handling**: Missing or deleted video files report a calm empty state with a replace/select prompt. Zero network transmission.

### 4. Focus Mode & Acoustic Ambient Sound
- **Standalone Focus Mode**: Start focus sessions directly without selecting a task, or launch from any Todo task.
- **Timer Presets**: Quick duration selectors: 5, 10, 15, 25, 45, 60 minutes + custom durations.
- **Real-Time Acoustic Noise**: Real-time synthesized low-pass filtered brown noise generator (`AmbientSoundPlayer`) via native `AudioTrack`. Zero audio asset file bloat, 100% offline.
- **Foreground Service Persistence**: Ongoing notification with monotonic clock math, countdown display, pause/resume, and cancel actions.
- **Reboot Resilience**: Timers survive app process death and device restarts.

### 5. Daily Progress Indicator
- **Embedded in Todo Screen**: Integrated directly into the main view (no cluttered separate analytics tabs).
- **Comprehensive Daily Metrics**:
  - Tasks completed today vs. total tasks due today.
  - Overall completion rate percentage.
  - Active goal check-ins completed today.
- **Edge-Case Resilience**: Seamlessly handles zero tasks due today, overdue tasks, tasks without due dates, and day/timezone transitions.

### 6. Four Redesigned Jetpack Glance Home-Screen Widgets
1. **Today's Tasks Widget** (`TodayTasksWidget`):
   - Generous 24dp rounded corners.
   - Dynamic 60:30:10 theme matching.
   - **Direct Interactivity**: Complete tasks directly from the home screen using `ToggleTaskActionCallback` without opening the app!
   - Quick-add shortcut (`+`).
   - Calm, encouraging empty state.
2. **Goal Progress Widget** (`GoalProgressWidget`):
   - Generous 24dp rounded corners.
   - **Direct Interactivity**: Check off daily goals directly on the home screen using `ToggleGoalActionCallback`!
   - Shows active goal count and today's progress.
3. **Active Timer Widget** (`ActiveTimerWidget`):
   - Live remaining time in large bold typography.
   - **Direct Controls**: Pause, Resume, and Cancel timer directly from the home screen widget using `TimerControlActionCallback`!
4. **Daily Overview Widget** (`DailyOverviewWidget`):
   - Dual summary cards showing Tasks and Goals completed today.
   - Overall daily completion percentage badge.

### 7. Todo Task Management
- **Today's Overview**: Live formatted date header and completion counter.
- **Quick-Add Bar**: Single-tap creation directly from the main view.
- **Task Properties**: Title, notes, priority (High, Medium, Low), due date, reminder timestamp, and ordering.
- **Interactive Checkbox**: Smooth toggle between uncompleted and completed states.
- **Expandable Completed Section**: Accordion toggle preserving focus on pending tasks.

### 8. Recurring Goals Tracker
- **Strict 6-Goal Limit**: Enforcing deliberate focus with an explanatory limit dialog.
- **Checkbox & Measurable Goals**: Daily checkmarks or numeric targets with `+1` increment buttons and progress bars.
- **Streaks & Analytics**: Current streak, best streak, completed days, missed days, and completion rate calculation.
- **Lifecycle**: Active, Paused, and Archived states.

### 9. 60:30:10 Design System & Custom Themes
- **Built-in Presets**:
  - **Daylight**: Light Ivory background (60%), Warm Neutral grey container (30%), Sage green accent (10%).
  - **Sandstone**: Warm Ochre background (60%), Dune container (30%), Terracotta Amber accent (10%).
  - **Midnight**: Deep Obsidian dark background (60%), Graphite container (30%), Nordic Frost Blue accent (10%).
  - **Slate**: Cool Slate dark background (60%), Muted Steel container (30%), Glacier Blue accent (10%).
  - **Rose**: Cashmere Rose background (60%), Dust Rose container (30%), Crimson Rose accent (10%).
- **Custom Theme Creator**: Pickers for 60% background, 30% container, and 10% accent with real-time preview card.

### 10. Local Data Architecture & Privacy
- **Local-Only Database**: Room Database 2.6.1 with relational entities and indices.
- **DataStore Preferences**: Fast coroutine-safe settings.
- **JSON Backup & Restore**: Export full database to JSON; import from JSON; complete data wipe action.

---

## ✦ Android Compatibility & Parsing Fixes

During testing on Android 11 (API 30), sideloading issues were investigated and permanently fixed:
1. **Manifest XML Compatibility**: Removed Android 14 `<property>` tags which cause `PackageParser` parse errors on Android 11. Used `foregroundServiceType="dataSync"`.
2. **Signature Schemes**: APKs are signed with dual **v1 (JAR signing)** and **v2/v3 (APK Signature Scheme)** schemes via `apksigner`. This satisfies both OEM Android 11 package installers requiring `META-INF/*.SF` and modern Android runtime integrity checks.
3. **Storage Permissions**: Exported APKs have open read permissions (`chmod 644`) to guarantee access for system installers across PRoot/Termux and host file managers.

---

## ✦ Build & Testing Instructions

### Prerequisites
- JDK 17
- Android SDK 34 (`platforms/android-34`, `build-tools/34.0.0`)

### Build Debug & Release APKs
```bash
cd /root/daymark
./gradlew assembleDebug assembleRelease
```

### Run Automated Tests
```bash
cd /root/daymark
./gradlew testDebugUnitTest
```

All test suites pass at 100%:
- `DailyProgressCalculatorTest`: Empty states, today vs overdue vs future tasks, goal check-in percentages, overall weighted rates.
- `DailyMotivationProviderTest`: Rotating quotes determinism, greetings, reflection prompts, completion encouragements.
- `TaskLogicTest`: Task lifecycle, completion toggles, ordering, duplication.
- `TimerLogicTest`: Monotonic timer calculation, pause/resume math, expiration on process death.
- `StreakCalculatorTest`: Streak continuation, missed-day preservation, completion rate formula.
- `GoalLimitAndLogicTest`: 6-active-goal limit enforcement, measurable target threshold.
- `ThemeAndContrastTest`: 60:30:10 palette token contrast, dark/light relative luminance calculations.

---

## ✦ Artifact Locations

| Artifact | Path | Size | Description |
|---|---|---|---|
| **Production APK** | `/sdcard/Download/Daymark.apk` | ~15.8 MB | Optimized, dual v1+v2+v3 signed APK |
| **Debug APK** | `/sdcard/Download/Daymark-debug.apk` | ~22.9 MB | Debuggable build for testing |
| **Release APK** | `/sdcard/Download/Daymark-release.apk` | ~15.8 MB | Release build copied to shared storage |
| **Brand Logo Asset** | `daymark_tick.png` | 1024×1024 PNG | High-resolution tick icon |
| **Test HTML Report** | `app/build/reports/tests/testDebugUnitTest/index.html` | 100% Passed | Automated test verification |
