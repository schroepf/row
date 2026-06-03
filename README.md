# row

Android sample application using:

- Jetpack Compose for the UI
- Ktor for REST calls
- AppAuth for the Concept2 OAuth login flow
- MVI-style state handling (intent, reducer, state)
- Unit tests and snapshot tests for QA

## Concept2 configuration

Provide the Concept2 OAuth configuration either through environment variables or in `/tmp/workspace/schroepf/row/local.properties`.
Environment variables override `local.properties`.

```properties
concept2.clientId=your-client-id
concept2.clientSecret=your-client-secret
concept2.redirectUri=your.app://oauth/callback
```

Required environment variables:

- `CONCEPT2_CLIENT_ID`
- `CONCEPT2_CLIENT_SECRET`
- `CONCEPT2_REDIRECT_URI`

## Run tests

```bash
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback ./gradlew :app:testDebugUnitTest
```

## Record snapshot references

```bash
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback ./gradlew :app:recordPaparazziDebug
```

## Verify snapshot tests

```bash
CONCEPT2_CLIENT_ID=test CONCEPT2_CLIENT_SECRET=test CONCEPT2_REDIRECT_URI=row://oauth/callback ./gradlew :app:verifyPaparazziDebug
```
