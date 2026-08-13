package de.mistatee.erglog.data.concept2.logbook.profile.api

import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile

/**
 * Talks to Concept2's `/api/users/{user}` endpoint to fetch the Logbook Account holder's profile.
 * See https://log.concept2.com/developers/documentation/#users.
 */
interface ProfileApi {
    suspend fun fetchProfile(accessToken: String): Profile
}
