package de.mistatee.erglog.data.concept2.logbook.profile

import de.mistatee.erglog.data.concept2.auth.AuthRepository
import de.mistatee.erglog.data.concept2.logbook.profile.api.ProfileApi
import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile
import de.mistatee.erglog.data.local.profile.ProfileDao
import de.mistatee.erglog.data.local.profile.toDomain
import de.mistatee.erglog.data.local.profile.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Concept2ProfileRepository(
    private val authRepository: AuthRepository,
    private val profileApi: ProfileApi,
    private val profileDao: ProfileDao,
) : ProfileRepository {
    override fun observeProfile(): Flow<Profile?> = profileDao.observe().map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> =
        authRepository.validSession().mapCatching { session ->
            val profile = profileApi.fetchProfile(session.accessToken)
            profileDao.upsert(profile.toEntity())
        }
}
