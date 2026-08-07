# Ship Concept2 OAuth client_secret inside the app

Concept2's Logbook API requires `client_secret` in the token-exchange request body and does not support PKCE, so a native Android app cannot keep it truly confidential — it can be extracted from the APK. We accept this risk and store the client_id/client_secret as build-time config (via `local.properties` → `BuildConfig`, gitignored) rather than standing up a backend proxy to hold the secret server-side. If abuse of the leaked secret becomes a problem, the fallback is a minimal backend that performs the code→token exchange on the app's behalf.
</content>
