package de.mistatee.erglog.data.profile

import de.mistatee.erglog.data.auth.AuthRepository

interface ProfileRepository {
    suspend fun fetchProfile(): Result<Profile>
}

class DefaultProfileRepository(
    private val authRepository: AuthRepository,
    private val profileApi: ProfileApi,
) : ProfileRepository {
    override suspend fun fetchProfile(): Result<Profile> =
        authRepository.validSession().mapCatching { session ->
            profileApi.fetchProfile(session.accessToken)
        }
}
