# Battery Notifier

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Material Design](https://img.shields.io/badge/Material_Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

> An Android application engineered to track battery charge levels in real time and deliver high-priority alerts when a user-defined threshold is reached, helping minimize overcharging and extend device battery longevity.

---

## Overview

Battery Notifier is designed for mobile users seeking to optimize lithium-ion battery lifecycle health. Modern battery chemistry deteriorates faster when sustained at high voltages (100% capacity) under elevated charging temperatures. By allowing users to set a customized target threshold (such as 80%), the application monitors charging status and dispatches high-priority notifications to disconnect the charger in a timely manner.

The application leverages native Android broadcast receivers, background services, and system alarm scheduling to balance responsiveness with power efficiency.

---

## Core Features

- **Live Battery Tracking**: Reads device battery status and renders current percentage levels directly in the user interface.
- **Configurable Target Threshold**: Provides an interactive slider with haptic feedback to set target thresholds between 1% and 100%.
- **High-Priority Notifications**: Uses a dedicated high-importance notification channel with optional full-screen intent triggers for locked screens.
- **Automated Power State Detection**: Listens to system power connection and disconnection events to manage monitoring workflows automatically.
- **Persistent Preferences**: Saves configured target levels and monitoring state across app sessions using `SharedPreferences`.
- **Material Design 3 Interface**: Adheres to modern Material Design guidelines with dynamic states and clear visual indicators.

---

## System Architecture

The application is structured into decoupled components handling UI, hardware broadcasts, background execution, and user notifications:

```
+-------------------------------------------------------------+
|                        MainActivity                         |
|  - Renders UI & slider controls                             |
|  - Handles runtime notification permissions (Android 13+)   |
|  - Listens to ACTION_BATTERY_CHANGED while in foreground     |
|  - Starts/Stops BatteryService Foreground Service           |
+------------------------------+------------------------------+
                               |
            +------------------+------------------+
            |                                     |
            v                                     v
+-----------------------+             +-----------------------+
| PowerConnectionReceiver|             |    BatteryService     |
| Listens to:           |             | - Ongoing Foreground  |
| - POWER_CONNECTED     |             |   Service (API 24-35) |
| - POWER_DISCONNECTED  |             | - Live battery updates|
+-----------+-----------+             +-----------+-----------+
            |                                     |
            |                                     v
            |                         +-----------------------+
            |                         |  NotificationHelper   |
            +------------------------>| - Ongoing Channel     |
                                      | - High-Priority Alert |
                                      +-----------------------+
```

### Component Breakdown

- **`MainActivity.kt`**: The primary user interface. Manages user target percentage input via Material Slider, handles dynamic runtime permission requests for `POST_NOTIFICATIONS` on Android 13+, reflects live battery levels in foreground, and manages service lifecycle.
- **`BatteryService.kt`**: An Android Foreground Service that runs reliably across Android versions (API 24 through API 35). Displays an ongoing, low-priority status notification while active and evaluates real-time battery status against the user's chosen threshold.
- **`NotificationHelper.kt`**: Creates dual notification channels (silent for foreground monitoring status, high-importance for target reached alerts) and manages notifications with action buttons and full-screen intents.
- **`PowerConnectionReceiver.kt`**: System broadcast receiver responding to `ACTION_POWER_CONNECTED` and `ACTION_POWER_DISCONNECTED` to manage monitoring state and dismiss alerts when the charger is unplugged.

---

## Technical Specifications

| Parameter | Specification |
|---|---|
| Target Platform | Android |
| Minimum SDK | API 24 (Android 7.0 Nougat) |
| Target SDK | API 35 (Android 15) |
| Compile SDK | API 35 |
| Primary Language | Kotlin 1.9.24 |
| UI Framework | Android Views / Material Components 1.12.0 |
| Build Toolchain | Android Gradle Plugin (AGP) 8.8.1 / Gradle 8.8.1 |
| Java Compatibility | JDK 11 |

---

## Permissions and Platform Considerations

The application requests the following permissions in `AndroidManifest.xml`:

- `android.permission.POST_NOTIFICATIONS`: Required on Android 13 (API level 33) and higher to display alert notifications.
- `android.permission.USE_FULL_SCREEN_INTENT`: Enables head-up, full-screen notifications when the device screen is locked.

### Background Execution & Power Optimization Notes

- **Doze Mode & App Standby**: Aggressive OEM battery savers and standard Android Doze mode may batch or delay alarms if the application is not excluded from battery optimization.
- **Runtime Permissions**: Devices running Android 13 or later must explicitly grant notification permissions at runtime for alert delivery.

---

## Prerequisites

Before building the project, verify that your environment contains:

- **Android Studio**: Ladybug (2024.2.1) or higher
- **JDK**: Version 11 or higher
- **Android SDK**: Platforms API 24 through API 35
- **Physical Device / Emulator**: Running Android API 24 or higher

---

## Installation and Build Setup

### 1. Clone Repository

```bash
git clone https://github.com/PramudithaN/Battery-Notifier.git
cd Battery-Notifier
```

### 2. Open Project

1. Launch Android Studio.
2. Select **File > Open** and choose the `Battery-Notifier` directory.
3. Allow Gradle to synchronize project dependencies.

### 3. Build via CLI

Use the Gradle wrapper to build the debug package:

**On Linux / macOS:**
```bash
./gradlew assembleDebug
```

**On Windows (PowerShell):**
```powershell
.\gradlew assembleDebug
```

---

## Available Gradle Tasks

| Command | Purpose |
|---|---|
| `./gradlew assembleDebug` | Compiles source files and generates the debug APK artifact |
| `./gradlew assembleRelease` | Compiles source files and generates the release APK artifact |
| `./gradlew installDebug` | Builds and installs the debug APK on an active ADB device |
| `./gradlew test` | Executes local unit tests |
| `./gradlew connectedAndroidTest` | Runs instrumented tests on an attached device or emulator |
| `./gradlew lint` | Analyzes source code for structural, performance, and API defects |
| `./gradlew clean` | Deletes generated build directories and cached outputs |

---

## Project Structure

```
Battery-Notifier/
├── app/                                 # Primary application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pramuditha/batterynotifier/
│   │   │   │   ├── BatteryService.kt          # Ongoing foreground monitoring service
│   │   │   │   ├── MainActivity.kt            # Main user interface & permission controller
│   │   │   │   ├── NotificationHelper.kt      # Dual-channel notification builder & alerts
│   │   │   │   └── PowerConnectionReceiver.kt # Charger connect/disconnect listener
│   │   │   ├── res/                           # Layouts, strings, colors, drawables
│   │   │   └── AndroidManifest.xml            # Application declarations and permissions
│   │   └── test/                              # Local unit test suite
│   ├── build.gradle.kts                       # App module configuration
│   └── proguard-rules.pro                     # R8/ProGuard shrinking rules
├── gradle/
│   ├── libs.versions.toml                     # Version catalog for dependencies
│   └── wrapper/                               # Gradle wrapper binaries and properties
├── build.gradle.kts                           # Root build configuration
├── settings.gradle.kts                        # Module inclusion definitions
└── gradlew.bat                                # Windows build executable
```

---

## Author & Contact

- **Author**: Pramuditha Nadun
- **GitHub**: [github.com/PramudithaN](https://github.com/PramudithaN)
- **LinkedIn**: [linkedin.com/in/pramuditha-nadun-612b1b204](https://linkedin.com/in/pramuditha-nadun-612b1b204)
- **Email**: pramudithanadun@gmail.com
