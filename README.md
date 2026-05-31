# row

Android sample application using:

- Jetpack Compose for the UI
- Ktor for REST calls
- MVI-style state handling (intent, reducer, state)
- Unit tests and snapshot tests for QA

## Run tests

```bash
./gradlew :app:testDebugUnitTest
```

## Run snapshot tests

```bash
./gradlew :app:testDebugUnitTest --tests "com.schroepf.row.RowScreenSnapshotTest"
```
