package de.mistatee.erglog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.mistatee.erglog.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = ${ProfileEntity.SINGLETON_ID}")
    fun observe(): Flow<ProfileEntity?>

    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}
