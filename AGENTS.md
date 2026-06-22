# AGENTS.md

Agent guidance for the `row` Android app repository.

## Environment setup

All Gradle tasks require Concept2 OAuth configuration. Provide via environment variables (preferred) or `local.properties`:

```bash
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback
```

The build fails immediately if any of these are missing. Use the test values above for local development and testing.

## Test commands

Run all tests in CI order:

```bash
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback \
  ./gradlew --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:verifyPaparazziDebug
```

Individual test tasks:

```bash
# Unit tests only
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback \
  ./gradlew :app:testDebugUnitTest

# Snapshot tests only (verify)
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback \
  ./gradlew :app:verifyPaparazziDebug

# Record new snapshot references (only when UI changes are intentional)
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback \
  ./gradlew :app:recordPaparazziDebug
```

**Important**: Always verify snapshots after recording. Changes to snapshots should be reviewed carefully in diffs.

## Architecture

- **Single module**: All code in `:app`
- **Package structure**: `com.schroepf.row` (main), `com.schroepf.row.api.auth`, `com.schroepf.row.api.log`
- **MVI pattern**: Intent → Reducer → State (see `RowMvi.kt`)
- **UI**: Jetpack Compose with Material3
- **Networking**: Ktor client with kotlinx.serialization
- **OAuth**: AppAuth for Concept2 login flow
- **Tests**: JUnit4 for unit tests, Paparazzi for snapshot tests

## Build quirks

- Build config values (`CONCEPT2_*`) are injected from env vars or `local.properties` at build time (see `app/build.gradle.kts:19-32`)
- The redirect URI scheme is automatically extracted and set as `manifestPlaceholders["appAuthRedirectScheme"]` for AppAuth
- Snapshots are stored in `app/src/test/snapshots/images/`
- JDK 17 required (configured in `build.gradle.kts`)

## CI

GitHub Actions runs on every PR and push to `main`:
- Assembles debug APK
- Runs unit tests
- Verifies snapshot tests

Secrets are configured in GitHub for CI (`CONCEPT2_CLIENT_ID`, `CONCEPT2_CLIENT_SECRET`, `CONCEPT2_REDIRECT_URI`).
