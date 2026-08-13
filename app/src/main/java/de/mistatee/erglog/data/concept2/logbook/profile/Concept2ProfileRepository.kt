package de.mistatee.erglog.data.concept2.logbook.profile

import de.mistatee.erglog.data.concept2.auth.AuthRepository
import de.mistatee.erglog.data.concept2.logbook.profile.api.ProfileApi
import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile

class Concept2ProfileRepository(
    private val authRepository: AuthRepository,
    private val profileApi: ProfileApi,
) : ProfileRepository {
    override suspend fun fetchProfile(): Result<Profile> =
        authRepository.validSession().mapCatching { session ->
            profileApi.fetchProfile(session.accessToken)
        }
}
