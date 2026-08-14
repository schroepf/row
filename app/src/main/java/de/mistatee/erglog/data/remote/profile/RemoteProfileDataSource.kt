package de.mistatee.erglog.data.remote.profile

import de.mistatee.erglog.data.model.Profile

/**
 * Talks to Concept2's `/api/users/{user}` endpoint to fetch the Logbook Account holder's profile.
 * See https://log.concept2.com/developers/documentation/#users.
 */
interface RemoteProfileDataSource {
    suspend fun fetchProfile(accessToken: String): Profile
}
