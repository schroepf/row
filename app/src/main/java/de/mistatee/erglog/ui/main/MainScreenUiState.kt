package de.mistatee.erglog.ui.main

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState

    /** Nothing cached yet, and the initial sync also failed — nothing to show the Logbook Account holder. */
    data class Error(val throwable: Throwable) : MainScreenUiState

    /**
     * Cached data is available and shown. [syncError] is non-null when the most recent attempt to
     * refresh from Concept2 failed (e.g. no network) — the shown data may be [Stale]. See
     * CONTEXT.md.
     */
    data class Success(val username: String, val syncError: Throwable? = null) : MainScreenUiState
}
