# AGENTS.md

Orientation for anyone (human or agent) changing this repo.

## Shape

- Single module `:app`. Framework in `app/src/main/kotlin/com/justan/shared`, product code in
  `com.justan.lowkey`, tests in `app/src/test/kotlin`.
- Kotlin only. Java 21 toolchain. Versions are pinned in `gradle/libs.versions.toml` - never inline
  a version in `build.gradle.kts`.

## Hard rules

- **No `Thread.sleep`** in screens or tests. Waits go through `PollingWaitStrategy`; the only
  sleeper is injected for unit tests.
- **No locators in tests.** Every selector lives in a `*ViewResource` object and is used from a screen.
- **No cross-platform screen abstractions.** Android and iOS screens are separate classes; sharing
  happens only in `shared/`.
- **Screens navigate, tests assert.** `isDisplayed` inside a screen is a precondition, not a result.
- **Tests are independent.** No ordering, no shared mutable state between tests.
- **`@Disabled` requires a ticket id** (`LOCAL-12 ...`). `./gradlew verifyTestAnnotations` enforces it.
- **Secrets never in code or config.** Use the env override and `secrets/` (git-crypt).

## Before you push

```bash
./gradlew build -x test        # compile + detekt
./gradlew pr                   # device-free suite
./gradlew verifyTestAnnotations
```

## Where to read next

- `app/src/main/AGENTS.md` - screens, waits, API clients
- `app/src/test/AGENTS.md` - base classes, tags, lifecycle, flaky policy
