# Use Chrome Custom Tabs for the OAuth login page

The Authorization Code flow requires showing Concept2's hosted login/consent page and receiving a redirect back into the app. We use Chrome Custom Tabs (via `androidx.browser`) rather than an in-app WebView, following RFC 8252 guidance for native app OAuth: Custom Tabs share the system browser's session/cookies and are trusted by the user as "really" being Concept2's page, whereas an app-controlled WebView cannot make either guarantee and is the pattern OAuth providers explicitly warn against.
</content>
