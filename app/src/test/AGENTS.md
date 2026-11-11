# app/src/test - tests

## Base classes

- `AndroidLowKeyTest` (abstract, `@TestInstance(PER_CLASS)`): starts Appium, resolves and installs the
  APK, opens the session, then for each test starts `logcat` capture and `screenrecord`. Artifacts are
  kept only when the test failed (the extension sets the flag, `@AfterEach` reads it). `@AfterAll`
  quits the driver and stops the server.
- `IosLowKeyTest`: same lifecycle for iOS; not runnable here.

## Suite split

- `SMOKE` - critical user paths, the ones that block a release. Keep this list short and stable.
- `T0` - UI behaviour and sub-functions of a feature (menus, dialogs, editors, settings).
- `T1`/`T2` - deeper or slower scenarios, edge cases, cross-feature combinations.

## Test isolation

Every test creates its own project through the UI (`createProject(uniqueProjectName(...))`) and removes
it in a `finally { cleanupProject(name) }`. `cleanupProject` goes home first and never throws, so a
failing assertion is never masked by cleanup.

## Required annotations

- Class: `@Tag("LOWKEY")`, `@Tag("ANDROID")` (or `IOS`) come from the base class; add the feature tag,
  e.g. `@Tag("RECORDING")`.
- Method: exactly one tier - `@Tag("SMOKE")`, `@Tag("T0")`, `@Tag("T1")` or `@Tag("T2")`. Add
  `@Tag("PR-CHECK")` only for tests that need no device.
- `@DisplayName` in plain language; the method name is fine as the technical name.

## Lifecycle rules

- Never start or stop Appium, the driver or captures yourself - the base class owns them.
- A test must leave the app in a state the next test can start from: navigate home at the end if you
  opened a dialog, or rely on `fullReset` on the next session (default).
- Assertions use JUnit `Assertions`; message text should say what the user expected.

## Flaky tests

- Disable with `@Disabled("<TICKET>-<n> <reason>")` and leave the body intact so it can be re-enabled.
- Never delete a flaky test; never silence it with a longer sleep.
- `./gradlew verifyTestAnnotations` fails when the ticket id is missing.

## Artifacts

Failure screenshots, logcat and recordings land in `build/artifacts/`. A passing test leaves nothing
behind on purpose - disk stays bounded on a device runner.
