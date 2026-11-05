# TVForge

A lightweight, remote-friendly home screen for standard Android TV devices.

**Current scope:** Android TV 11+ (API 30+). Jio set-top boxes are not included in the initial compatibility target and have not been tested.

## Features in v1.0.0

- Android TV launcher entry point (`LEANBACK_LAUNCHER` and Home intent).
- Discovers apps that expose Android TV or standard launcher activities.
- Launches apps using the system package manager.
- Displays device manufacturer/model, Android version, available RAM and app-volume storage information.
- D-pad-friendly native Android buttons.
- No root, ADB, or firmware modification required to run.

## Requirements

- Android Studio with Android SDK Platform 35.
- JDK 17.
- Gradle via Android Studio's Gradle integration.
- An Android TV emulator or physical Android TV device running Android 11 or newer.

## Build

1. Open this repository in Android Studio.
2. Allow Gradle to sync and install the required Android SDK.
3. Select the `app` run configuration.
4. Build with **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK is normally written to `app/build/outputs/apk/debug/app-debug.apk`.

Command-line build (after installing Android SDK and Gradle):

```bash
gradle assembleDebug
```

## Install

Install through Android Studio onto a test device, or use a file manager/USB sideloading flow if the TV firmware permits installation from unknown sources. The launcher does not require ADB at runtime. ADB is only an optional developer installation route.

After installation, press Home and select TVForge if Android presents a launcher chooser. Some TV manufacturers restrict changing the default Home app.

## Compatibility

See [COMPATIBILITY.md](COMPATIBILITY.md). Android TV implementations vary by manufacturer. App visibility depends on package visibility and firmware behavior. Android 11+ package visibility may require explicit queries or broader package visibility; this project declares `QUERY_ALL_PACKAGES` for its core app-discovery function.

## Privacy

TVForge does not collect analytics, transmit device information, or contact a server. Device information is displayed locally.

## Safety

TVForge does not use root, exploit device-management services, disable system packages, modify firmware, or change security settings.

## Status

This is an initial source release. It has not yet been validated on a range of physical televisions. Please test on an emulator first and report device-specific issues.

## License

MIT. See [LICENSE](LICENSE).
