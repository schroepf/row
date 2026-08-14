package de.mistatee.erglog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.mistatee.erglog.data.local.profile.ProfileDao
import de.mistatee.erglog.data.local.profile.ProfileEntity
import de.mistatee.erglog.data.local.results.ResultDao
import de.mistatee.erglog.data.local.results.ResultEntity
import de.mistatee.erglog.data.local.results.ResultSyncStateDao
import de.mistatee.erglog.data.local.results.ResultSyncStateEntity

private const val DATABASE_NAME = "de.mistatee.erglog.database"

/**
 * The app's offline-first source of truth: cached Result History, Profile, and the Result
 * History's sync progress. See docs/adr/0005-room-for-offline-first-persistence.md.
 */
@Database(
    entities = [ResultEntity::class, ProfileEntity::class, ResultSyncStateEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ErgLogDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao

    abstract fun resultSyncStateDao(): ResultSyncStateDao

    abstract fun profileDao(): ProfileDao

    companion object {
        fun create(context: Context): ErgLogDatabase =
            Room
                .databaseBuilder(context.applicationContext, ErgLogDatabase::class.java, DATABASE_NAME)
                // Room's first introduction to this app; no shipped schema to migrate from yet.
                // See docs/adr/0005-room-for-offline-first-persistence.md.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
