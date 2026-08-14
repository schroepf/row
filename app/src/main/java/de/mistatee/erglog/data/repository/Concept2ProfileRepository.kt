package de.mistatee.erglog.data.repository

import de.mistatee.erglog.data.local.dao.ProfileDao
import de.mistatee.erglog.data.local.entities.toDomain
import de.mistatee.erglog.data.local.entities.toEntity
import de.mistatee.erglog.data.model.Profile
import de.mistatee.erglog.data.remote.profile.RemoteProfileDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Concept2ProfileRepository(
    private val authRepository: AuthRepository,
    private val remoteProfileDataSource: RemoteProfileDataSource,
    private val profileDao: ProfileDao,
) : ProfileRepository {
    override fun observeProfile(): Flow<Profile?> = profileDao.observe().map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> =
        authRepository.validSession().mapCatching { session ->
            val profile = remoteProfileDataSource.fetchProfile(session.accessToken)
            profileDao.upsert(profile.toEntity())
        }
}
