# Toolchain upgrade runbook

Upgrade in this order. Each step depends on the one before it; skipping ahead produces failures that
look like app bugs.

1. **Node** - bump `.nvmrc`, run `nvm install && nvm use`. Appium requires the pinned major.
2. **Appium server** - `npm i -g appium@3` (or the next major), then `appium -v`. Never `npm install`
   inside `APPIUM_HOME`.
3. **Drivers** - `appium driver update uiautomator2` and/or `appium driver update xcuitest`.
   `DriverVersionCheck` enforces the minimum and the message names the command.
4. **WDA** - rebuild and re-sign for the device's iOS version: clean
   `~/Library/Developer/Xcode/DerivedData`, then let Appium build it once with
   `ENABLE_XCODE_DEBUG=true` so signing errors are visible.
5. **Device OS** - only after the stack above is green. A fresh OS often resets animation scales and
   permissions; `AndroidDeviceSettingsManager` and `PermissionUtils` re-apply them.

## Reading failures

- `xcodebuild failed with code 65` is never the real error. It means WDA, the device or Xcode are out
  of sync - read the Appium server log (`build/appium-logs/`) for the first error above it.
- `Could not start a new session ... Neither ANDROID_HOME ...` - SDK path missing for the server
  process, see FAQ.
- `Appium driver uiautomator2 X is older than required Y` - step 3.
- Session created but the wrong screen appears - the app changed, not the toolchain. Update the
  `*ViewResource` constants.
