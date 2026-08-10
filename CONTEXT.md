# ErgLog

ErgLog is an Android app for viewing rowing/ergometer workout history from a user's Concept2 Logbook account.

## Language

**Logbook Account**:
A user's identity on Concept2's Logbook service, external to this app. The app never sees the account's password.
_Avoid_: Concept2 account, user account

**Client Credentials**:
The Client ID and Client Secret that identify this app to Concept2, issued once when the app is registered with Concept2.
_Avoid_: API key, app credentials

**Authorization Code**:
A one-time code Concept2 hands back to the app after the Logbook Account holder approves access, exchanged once for an Access Token and Refresh Token.
_Avoid_: auth code, grant code

**Access Token**:
A short-lived credential used to authenticate calls to the Logbook API on behalf of a Logbook Account.
_Avoid_: bearer token, auth token

**Refresh Token**:
A long-lived credential (about a year) exchanged for a new Access Token once the current one expires, without involving the Logbook Account holder again.
_Avoid_: renewal token

**Scope**:
A named permission (e.g. `user:read`, `results:read`) that limits what an Access Token can be used for. Requesting more Scopes than needed risks the Logbook Account holder rejecting authorization.
_Avoid_: permission, access level

**Session**:
The app's local record that a Logbook Account holder is currently authenticated, backed by a stored Access Token and Refresh Token. Its absence means the holder must go through authorization again.
_Avoid_: login state, auth state

**Profile**:
The Logbook Account holder's profile data (username, first/last name, email, etc.) as returned by Concept2's `/api/users/{user}` endpoint.
_Avoid_: User, Account Details

**Valid Session**:
A [Session](#session) whose Access Token is confirmed usable right now — either still fresh, or freshly refreshed. Distinct from merely having *a* Session on record, which may hold a stale Access Token.
</content>
