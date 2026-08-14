package de.mistatee.erglog.data.repository

import de.mistatee.erglog.data.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Observes the Logbook Account holder's cached Profile, backed by Room. `null` until first synced. */
    fun observeProfile(): Flow<Profile?>

    /** Fetches the current Profile from Concept2 and writes it into the local cache. */
    suspend fun refresh(): Result<Unit>
}

