package de.mistatee.erglog.data.local.results

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single row tracking how far the cached [ResultEntity] rows have been synced with Concept2's
 * page-numbered `/api/users/me/results` endpoint. There is exactly one Result History per Logbook
 * Account holder, so this is a global sync-state row rather than a per-item remote key.
 */
@Entity(tableName = "result_sync_state")
data class ResultSyncStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val lastLoadedPage: Int,
    val totalPages: Int,
    val endOfPaginationReached: Boolean,
) {
    /** The next page to fetch, or `null` if the full Result History has already been synced. */
    val nextPage: Int? get() = if (endOfPaginationReached) null else lastLoadedPage + 1

    companion object {
        const val SINGLETON_ID = 0
    }
}
