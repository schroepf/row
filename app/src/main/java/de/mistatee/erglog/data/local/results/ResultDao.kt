package de.mistatee.erglog.data.local.results

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY date DESC, id DESC")
    fun pagingSource(): PagingSource<Int, ResultEntity>

    @Upsert
    suspend fun upsertAll(results: List<ResultEntity>)

    @Query("DELETE FROM results")
    suspend fun clear()
}
