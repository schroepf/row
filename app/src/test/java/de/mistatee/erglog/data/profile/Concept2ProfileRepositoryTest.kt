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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class Concept2ProfileRepositoryTest {
    @Test
    fun `fetchProfile returns profile on success`() = runTest {
        // given
        val session = MockData.Auth.session
        val profile = MockData.Api.Profile.profile

        val repository = Concept2ProfileRepository(
            authRepository = mockk {
                coEvery { validSession() } returns Result.success(session)
            },
            profileApi = mockk {
                coEvery { fetchProfile(session.accessToken) } returns profile
            },
        )

        // when
        val result = repository.fetchProfile()

        // then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(profile)
    }

    @Test
    fun `fetchProfile returns failure on missing session`() = runTest {
        // given
        val repository = Concept2ProfileRepository(
            authRepository = mockk {
                coEvery { validSession() } returns Result.failure(SessionError.NoSession)
            },
            profileApi = mockk(),
        )

        // when
        val result = repository.fetchProfile()

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<SessionError.NoSession>()
    }

    @Test
    fun `fetchProfile returns failure when API throws`() = runTest {
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
        )

        // when
        val result = repository.fetchProfile()

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(apiFailure)
    }
}
