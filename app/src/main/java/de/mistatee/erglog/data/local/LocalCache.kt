package de.mistatee.erglog.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The app's locally cached data (Result History, Profile, sync state), scoped to the active Session. */
interface LocalCache {
    /** Wipes all locally cached data, e.g. because the Logbook Account holder logged out. */
    suspend fun clear()
}

class RoomLocalCache(private val database: ErgLogDatabase) : LocalCache {
    override suspend fun clear() = withContext(Dispatchers.IO) { database.clearAllTables() }
}
