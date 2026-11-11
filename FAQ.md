# FAQ

**The suite cannot find my device.**
`adb devices -l` must list it as `device` (not `unauthorized`). If several are attached, set
`DEVICE_UDID` - otherwise the first *physical* device wins and emulators are rejected while
`requirePhysicalDevice` is true.

**"Neither ANDROID_HOME nor ANDROID_SDK_ROOT was exported"**
Appium needs the SDK. `AppiumManager` injects it into the server subprocess when it finds
`platform-tools` under `$ANDROID_HOME`, `$ANDROID_SDK_ROOT` or `~/Library/Android/sdk`. If your SDK is
elsewhere, export `ANDROID_HOME` before running Gradle.

**Appium will not start / port 4723 is busy.**
A leftover server is usually fine - the framework reuses anything listening on the port. To clean up:
`lsof -ti :4723 | xargs kill`. Use `-DskipAppiumKill=true` when you want to keep the server for manual
poking.

**`INSTALL_FAILED_TEST_ONLY` when the session is created.**
Debug APKs are marked test-only. The session options already pass `allowTestPackages=true`; if a
device still refuses, install once with `adb install -r -t <apk>` and the next run picks it up.

**A test passed on retry - should I trust it?**
No. Retries exist to survive driver hiccups, and the plugin is configured with
`failOnPassedAfterRetry = false`, so a retried pass still fails the suite. Treat it as flaky and fix or
disable it with a ticket.

**Where did my logcat and recording go?**
They are deleted for passing tests on purpose, to keep a device runner's disk bounded. Artifacts are
kept only when a test fails; `./gradlew debug-suite` includes a deliberately broken locator that shows
the failure path end to end.

**Why is there a `@Disabled` test for iOS?**
`IosHomeSmokeTest` compiles and is tagged, but this machine has no iOS device, WDA signing or
`xcuitest` driver. It cites `LOCAL-1` so `verifyTestAnnotations` accepts it.

**A test needs a project that already has clips - how is that handled?**
Every test builds its own state through the UI: `createProject(uniqueProjectName(...))`, record what it
needs, and delete the project in `finally`. Nothing depends on what a previous run left behind, even
after a hard failure.

**Can I run this against an emulator?**
Yes: `REQUIRE_PHYSICAL_DEVICE=false` and `DEVICE_UDID=emulator-5554`. Camera behaviour on an emulator
is synthetic, so results are indicative only.

**Why no Qase?**
LowKey has no test-management project. Tags and JUnit XML carry the reporting; wiring Qase later means
adding the reporter agent to the test tasks and a `@QaseId` check.
