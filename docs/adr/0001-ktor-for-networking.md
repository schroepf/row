# Use Ktor Client for networking

The app has no networking library yet and needs to call the Concept2 Logbook API (OAuth token exchange, later result fetching). We chose Ktor Client over Retrofit/OkHttp for its coroutine-native suspend API and to keep the door open for Kotlin Multiplatform if the app ever targets other platforms. Retrofit+OkHttp is the more conventional choice for a plain Android app and has more Android-specific tutorials/examples, so this trades some ecosystem familiarity for Kotlin-first ergonomics.
</content>
