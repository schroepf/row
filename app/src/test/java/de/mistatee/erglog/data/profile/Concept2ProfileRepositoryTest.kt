package de.mistatee.erglog.data.profile

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.auth.model.SessionError
import de.mistatee.erglog.data.concept2.logbook.profile.Concept2ProfileRepository
import de.mistatee.erglog.data.concept2.logbook.profile.model.ProfileException
import de.mistatee.erglog.data.local.profile.ProfileDao
import de.mistatee.erglog.data.local.profile.ProfileEntity
import de.mistatee.erglog.data.local.profile.toEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class Concept2ProfileRepositoryTest {
    @Test
    fun `refresh writes fetched profile into the local cache on success`() = runTest {
        // given
        val session = MockData.Auth.session
        val profile = MockData.Api.Profile.profile
        val profileDao = mockk<ProfileDao>(relaxUnitFun = true)

        val repository = Concept2ProfileRepository(
            authRepository = mockk {
                coEvery { validSession() } returns Result.success(session)
            },
            profileApi = mockk {
                coEvery { fetchProfile(session.accessToken) } returns profile
            },
            profileDao = profileDao,
        )

        // when
        val result = repository.refresh()

        // then
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { profileDao.upsert(profile.toEntity()) }
    }

    @Test
    fun `refresh returns failure on missing session without writing to the cache`() = runTest {
        // given
        val profileDao = mockk<ProfileDao>(relaxUnitFun = true)
        val repository = Concept2ProfileRepository(
            authRepository = mockk {
                coEvery { validSession() } returns Result.failure(SessionError.NoSession)
            },
            profileApi = mockk(),
            profileDao = profileDao,
        )

        // when
        val result = repository.refresh()

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<SessionError.NoSession>()
        coVerify(exactly = 0) { profileDao.upsert(any()) }
    }

    @Test
    fun `refresh returns failure when API throws`() = runTest {
        // given
        val session = MockData.Auth.session
        val apiFailure = ProfileException("http_500", null)

        val repository = Concept2ProfileRepository(
            authRepository = mockk {
                coEvery { validSession() } returns Result.success(session)
            },
            profileApi = mockk {
                coEvery { fetchProfile(session.accessToken) } throws apiFailure
            },
            profileDao = mockk(),
        )

        // when
        val result = repository.refresh()

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(apiFailure)
    }

    @Test
    fun `observeProfile emits the cached profile mapped to the domain model`() = runTest {
        // given
        val cachedProfile = ProfileEntity(username = "rower1")
        val repository = Concept2ProfileRepository(
            authRepository = mockk(),
            profileApi = mockk(),
            profileDao = mockk {
                every { observe() } returns flowOf(cachedProfile)
            },
        )

        // when
        val profile = repository.observeProfile().first()

        // then
        assertThat(profile).isNotNull()
        assertThat(profile?.username).isEqualTo(cachedProfile.username)
    }

    @Test
    fun `observeProfile emits null when nothing has been cached yet`() = runTest {
        // given
        val repository = Concept2ProfileRepository(
            authRepository = mockk(),
            profileApi = mockk(),
            profileDao = mockk {
                every { observe() } returns flowOf(null)
            },
        )

        // when
        val profile = repository.observeProfile().first()

        // then
        assertThat(profile).isEqualTo(null)
    }
}
