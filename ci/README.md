# ci

GitHub Actions owns the pipelines, so the runnable workflow files live in `.github/workflows/`:

| file | purpose |
| --- | --- |
| `.github/workflows/android-e2e.yml` | device suite: smoke first, then T0 when smoke is green |
| `.github/workflows/pr-check.yml` | `./gradlew pr` (tag `PR-CHECK`, no device) |
| `.github/workflows/lint.yml` | detekt + `verifyTestAnnotations` |

`unlock-secrets.sh` is the one shared step both the device pipeline and any future Jenkins job call.

The device job assumes a **self-hosted runner** with a physical phone attached and authorised
(`adb devices` shows `device`). A hosted runner cannot reach a USB device.
