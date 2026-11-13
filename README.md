# lowkey-automation

Appium + Kotlin + JUnit 5 end-to-end suite for **LowKey** (`com.justan.lowkeycam`), the Android
vlog camera. One Gradle module (`:app`), framework code in `app/src/main/kotlin`, tests in
`app/src/test/kotlin`.

Android is fully wired and verified on a physical Pixel 10a. iOS is scaffolded: screens, config and a
base test compile, but the only iOS test is `@Disabled("LOCAL-1 ...")` until a device with a signed
WDA and the `xcuitest` driver exists.

## Prerequisites

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 21 | `java -version` must report 21 |
| Node | 22+ | pinned by `.nvmrc` |
| Appium | 3.x | `npm i -g appium@3` (never `npm install` inside `APPIUM_HOME`) |
| uiautomator2 driver | 4.0.0+ | `appium driver install uiautomator2` (installs 8.x today) |
| Android SDK | platform-tools on `PATH` | `adb devices` must show your phone as `device` |
| Xcode + WDA | 15+ | iOS only; `xcodebuild` and a signing team in `ios/ios.conf` |

Verify the toolchain:

```bash
adb devices -l              # a physical device must show "device"
appium driver list --installed
./gradlew build -x test
```

## Configuration

Everything lives in `app/src/main/resources/` as HOCON. Every value CI changes has an env override,
so nothing needs editing on the runner:

| file | keys you will touch | env override |
| --- | --- | --- |
| `appium.conf` | server URL, locator timeout, poll interval, kill-on-exit | `APPIUM_SERVER_URL`, `LOCATOR_TIMEOUT_MS`, `APPIUM_KILL_ON_EXIT` |
| `android/android.conf` | `appPackage`, `appActivity`, APK path, device udid | `APP_SOURCE`, `DEVICE_UDID`, `ENABLE_SCREEN_RECORDING`, `REQUIRE_PHYSICAL_DEVICE` |
| `ios/ios.conf` | bundle id, `.app` path, WDA bootstrap, team | `IOS_BUNDLE_ID`, `APP_SOURCE`, `WDA_BOOTSTRAP_PATH`, `APPLE_TEAM_ID`, `ENABLE_XCODE_DEBUG` |
| `testdata.conf` | record duration, crop timeout, project names | - |
| `backend.conf` | base URL for setup APIs (empty for LowKey) | `BACKEND_BASE_URL` |
| `artifactory.conf` | artifact repo + pattern | `ARTIFACT_REPO_URL`, `ARTIFACT_VERSION` |

`DEVICE_UDID` is optional: left empty, the first **physical** device from `adb devices -l` is used and
emulators are refused unless `REQUIRE_PHYSICAL_DEVICE=false`.

The APK defaults to `../lowkey-cam/app/build/outputs/apk/debug/app-debug.apk`. Point `APP_SOURCE` at a
downloaded build to test something else.

Secrets never live in the repo: see [SECRETS.md](SECRETS.md).

## Suites

| Task | Tag expression | Device? |
| --- | --- | --- |
| `./gradlew lowkey-android` | `LOWKEY & ANDROID` | yes |
| `./gradlew lowkey-android-smoke` | `LOWKEY & ANDROID & SMOKE` | yes |
| `./gradlew lowkey-android-t0` | `LOWKEY & ANDROID & T0` | yes |
| `./gradlew lowkey-ios-smoke` | `LOWKEY & IOS & SMOKE` | yes + WDA |
| `./gradlew pr` | `PR-CHECK` | no |
| `./gradlew debug-suite` | `DEBUG` | yes |

`./gradlew test` (and therefore `build`/`check`) runs only `PR-CHECK`, so it stays green on a machine
with no device. Device work is always the named suite tasks.

Retries are on everywhere (`maxRetries = 1`); a test that only passes on retry still fails the run so
flakes stay visible.

Run one test:

```bash
./gradlew lowkey-android --tests "com.justan.lowkey.android.recording.RecordClipSmokeTest"
```

Debug without killing your own Appium server:

```bash
./gradlew lowkey-android-smoke -DskipAppiumKill=true
```

Keep a session open while you poke at the device - `AppiumManager.start()` reuses a server that is
already listening on `127.0.0.1:4723`.

## Coverage map

**SMOKE** - critical paths only; if one of these fails the build is not shippable.

| Test | Flow |
| --- | --- |
| `HomeSmokeTest` | app launches to home with the project card, project list, quick-record and settings |
| `RecordLowKeyClipSmokeTest` | low-key take (portrait hold, 16:9 crop) lands in the project |
| `RecordStandardClipSmokeTest` | standard take from the home card lands in the current project |
| `PlayVlogSmokeTest` | Play vlog opens the player and returns to the project |
| `SettingsSmokeTest` | recording resolution can be changed and persists |

**T0** - UI behaviour and sub-functions.

| Test | Covers |
| --- | --- |
| `CreateProjectTest` | + New, empty state, delete |
| `RenameProjectTest` | tap title, rename dialog, header update |
| `DeleteProjectTest` | header overflow, confirm dialog, list refresh |
| `DeleteClipTest` | per-clip ⋮ menu, single delete |
| `DeleteAllClipsTest` | clear the project without losing it |
| `TrimClipTest` | editor, end handle drag, save, runtime shrinks |
| `RecapTest` | 2s recap trims every clip |
| `ExportVlogTest` | export stitches and reports "saved to gallery" |
| `CameraControlsTest` | mode label, lens flip, pause/resume freeze, close |
| `QuickRecordEntryTest` | home Low-key / Normal entry points open the matching mode |
| `ClipOutputTest` | the recorded file itself: 16:9, no rotation metadata, duration matches |
| `ExportOutputTest` | the exported gallery file exists in MediaStore and is 16:9 |
| `CameraChipsTest` | torch, blackout and lens chips toggle without losing the preview |
| `DialogCancelTest` | cancelling rename / clip delete / project delete changes nothing |
| `ReorderClipsTest` | Move down reorders the clip list (playback and export order) |
| `EditorPreviewTest` | Preview starts the clip and Pause stops it again |
| `PlayerAdvanceTest` | the vlog player advances from clip 1 to clip 2 |

Output assertions pull the real artifact off the device (`run-as` for the private clip dir, MediaStore
+ `adb pull` for exports) and probe it with `ffprobe`, so a passing test means a playable file, not
just a promising label.

Not automated, and why:

| Area | Reason |
| --- | --- |
| SAF music picker | system file dialog in another app context |
| Widgets (3x2 and 1x1) | needs a launcher host and a resize surface |
| Quick Settings tile | system UI, same class of problem |
| Drag-to-reorder gesture | long-press drag on a scaled track is not deterministic device to device; the Move up/down menu path is covered instead |
| Share clip | system share sheet |
| Landscape capture | `CameraActivity` is `fullSensor`, so the OS follows the accelerometer: `setScreenOrientation` is ignored while the node is "locked programmatically". Only a physical turn exercises it. |

## What a run produces

| Path | Contents |
| --- | --- |
| `app/build/test-results/` | JUnit XML per class + `suite-summary.txt` |
| `app/build/reports/tests/` | HTML report |
| `build/artifacts/logs/` | `adb logcat` per test - **kept only on failure** |
| `build/artifacts/recordings/` | `screenrecord` per test - **kept only on failure** |
| `build/artifacts/failures/` | failure screenshots and suite-setup dumps |
| `build/appium-logs/` | one Appium server log per run |

`./gradlew postSlackResults` summarises the XML and posts to Slack; with no token it prints the
message instead (dry-run).

## Layout

```
app/src/main/kotlin/com/justan/
  shared/            client-neutral framework (appium, waits, screens, device utils, network, ci)
  lowkey/            product code: driver manager + per-platform screens and view resources
app/src/test/kotlin/com/justan/
  lowkey/android/    base class, features (recording/, ...)
  lowkey/ios/        scaffolded base class + disabled smoke test
  shared/            pure unit tests (no device)
  extensions/        artifacts + suite summary
```

## Adding a test

1. Put it in the feature package (`recording/`, `settings/`, ...), not a "smoke" package.
2. Extend `AndroidLowKeyTest` (which owns server, session and capture).
3. Tag the class with the feature area and the method with `SMOKE`, `T0`, `T1` or `T2`.
4. Drive the UI through screens; assert only in the test.

See [app/src/test/AGENTS.md](app/src/test/AGENTS.md) for the full rules.

## Troubleshooting

See [FAQ.md](FAQ.md) and the [toolchain upgrade runbook](docs/toolchain-upgrade.md).
