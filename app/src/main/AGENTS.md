# app/src/main - framework code

## Appium

- `shared/appium/AppiumManager` is a singleton: it starts a local server on first use, reuses one
  already listening on the configured port, tracks the live driver, and stops everything at the end.
  `-DskipAppiumKill=true` leaves the server running for debugging.
- It injects `ANDROID_HOME`/`ANDROID_SDK_ROOT` and the npm global bin into the Appium subprocess; a
  test JVM started from an IDE has neither.
- `DriverVersionCheck` fails fast when uiautomator2/xcuitest is older than the framework expects.

## Screens

- One class per screen per platform, extending `BaseScreen(driver, ScreenOs)`.
- `screenName` is used in logs; `isDisplayed(waitTimeMillis, screenshotOnFailure)` is mandatory and
  must be a real readiness check, not "any view exists".
- Navigation methods return the next screen so a flow reads as a chain:
  `home.openFirstProject().startRecording()`.
- Selectors live in `<X>ViewResource` and are `LocatorStrategy.Id`, `AccessibilityId`, `Text`,
  `TextContains`, `XPath`, `AndroidUiAutomator` or `IosPredicate`. Prefer accessibility ids; use
  XPath last.

## Waits

- `PollingWaitStrategy` polls every 500 ms (configurable), tolerates `NoSuchElement` and
  `StaleElementReference`, and throws `WaitTimeoutException` with the description and timeout.
- `Locators` wraps the driver: `element`, `findOrNull`, `exists`, `click`, `textOf`, `waitForGone`.
  A failed lookup triggers the screen's screenshot hook.

## Device and network helpers

- `shared/android`: `AdbUtils` (devices, install/uninstall, shell, push/pull), `DeviceUtils`
  (physical-device resolution), `AndroidDeviceSettingsManager` (animations off, stay awake),
  `PermissionUtils`, `AndroidLogCapture`, `AndroidScreenRecorder`.
- `shared/ios`: `IosDeviceUtils` (libimobiledevice), `IosDeviceSettingsManager`, `IosLogCapture`.
- `shared/network/RetrofitClient` builds Retrofit from `backend.conf` with a cookie store and auth
  header redaction. LowKey has no backend: `isConfigured` is false and nothing calls it.
- `shared/artifacts/TestArtifactManager` resolves the APK locally or downloads it from the artifact
  repo into `build/downloads`.
