package de.mistatee.erglog.data.profile

import de.mistatee.erglog.data.auth.AuthRepository
import de.mistatee.erglog.data.auth.Session
import de.mistatee.erglog.data.auth.SessionError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class Concept2ProfileRepositoryTest {
    @Test
    fun fetchProfile_onSuccess_returnsProfile() =
        runTest {
            val authRepository = mockk<AuthRepository>()
            coEvery { authRepository.validSession() } returns Result.success(SAMPLE_SESSION)
            val profileApi = mockk<ProfileApi>()
            coEvery { profileApi.fetchProfile(SAMPLE_SESSION.accessToken) } returns SAMPLE_PROFILE
            val repository = Concept2ProfileRepository(authRepository, profileApi)

            val result = repository.fetchProfile()

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_PROFILE, result.getOrNull())
        }

    @Test
    fun fetchProfile_whenNoValidSession_propagatesFailure() =
        runTest {
            val authRepository = mockk<AuthRepository>()
            coEvery { authRepository.validSession() } returns Result.failure(SessionError.NoSession)
            val profileApi = mockk<ProfileApi>()
            val repository = Concept2ProfileRepository(authRepository, profileApi)

            val result = repository.fetchProfile()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SessionError.NoSession)
        }

    @Test
    fun fetchProfile_whenApiThrows_propagatesFailure() =
        runTest {
            val apiFailure = ProfileException("http_500", null)
            val authRepository = mockk<AuthRepository>()
            coEvery { authRepository.validSession() } returns Result.success(SAMPLE_SESSION)
            val profileApi = mockk<ProfileApi>()
            coEvery { profileApi.fetchProfile(SAMPLE_SESSION.accessToken) } throws apiFailure
            val repository = Concept2ProfileRepository(authRepository, profileApi)

            val result = repository.fetchProfile()

            assertTrue(result.isFailure)
            assertEquals(apiFailure, result.exceptionOrNull())
        }

    private companion object {
        val SAMPLE_SESSION =
            Session(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                accessTokenExpiry = Instant.now().plusSeconds(3600),
            )
        val SAMPLE_PROFILE = Profile(username = "rower1")
    }
}
