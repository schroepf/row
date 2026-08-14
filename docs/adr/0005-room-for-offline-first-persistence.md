# Use Room as the offline-first source of truth for Result History and Profile

The app needs to work offline, showing the Logbook Account holder's last-known Result History and Profile even without network access. We adopted Room as the single source of truth for both: the UI and ViewModels read exclusively from Room, and a sync layer (a `RemoteMediator` for the paged Result History, a simple fetch-and-upsert for Profile) is the only code that talks to the Concept2 API. This is more machinery than a cache-aside fallback, but it plays natively with Paging 3's `RemoteMediator` and gives the UI reactive updates whenever a background sync completes.

Because the Concept2 Results API paginates by page number only (`current_page`/`total_pages`), with no per-item cursor, the `RemoteMediator`'s pagination state is tracked in a single global `ResultSyncState` row rather than a per-item `RemoteKeys` table — the latter is the idiomatic shape for cursor-based APIs and doesn't fit page-number pagination.

Schema migrations use `fallbackToDestructiveMigration()` for now, since this is Room's first introduction and there's no shipped schema to preserve; this should be revisited once the app has shipped a version with real user data worth migrating.

Cached data is wiped from Room on logout, since it's scoped to the currently authenticated Logbook Account and the device could later be used to log into a different account.
