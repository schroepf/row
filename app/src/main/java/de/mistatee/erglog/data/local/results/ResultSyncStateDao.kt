package de.mistatee.erglog.data.local.results

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ResultSyncStateDao {
    @Query("SELECT * FROM result_sync_state WHERE id = ${ResultSyncStateEntity.SINGLETON_ID}")
    suspend fun get(): ResultSyncStateEntity?

    @Upsert
    suspend fun upsert(state: ResultSyncStateEntity)

    @Query("DELETE FROM result_sync_state")
    suspend fun clear()
}
