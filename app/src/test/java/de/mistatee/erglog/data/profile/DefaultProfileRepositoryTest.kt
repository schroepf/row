package de.mistatee.erglog.data.profile

import de.mistatee.erglog.data.auth.AuthRepository
import de.mistatee.erglog.data.auth.Session
import de.mistatee.erglog.data.auth.SessionError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DefaultProfileRepositoryTest {
    @Test
    fun fetchProfile_onSuccess_returnsProfile() =
        runTest {
            val repository =
                DefaultProfileRepository(
                    FakeAuthRepository(validSessionResult = { Result.success(SAMPLE_SESSION) }),
                    FakeProfileApi(fetchProfileResult = { SAMPLE_PROFILE }),
                )

            val result = repository.fetchProfile()

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_PROFILE, result.getOrNull())
        }

    @Test
    fun fetchProfile_whenNoValidSession_propagatesFailure() =
        runTest {
            val repository =
                DefaultProfileRepository(
                    FakeAuthRepository(validSessionResult = { Result.failure(SessionError.NoSession) }),
                    FakeProfileApi(fetchProfileResult = { error("should not be called") }),
                )

            val result = repository.fetchProfile()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SessionError.NoSession)
        }

    @Test
    fun fetchProfile_whenApiThrows_propagatesFailure() =
        runTest {
            val apiFailure = ProfileException("http_500", null)
            val repository =
                DefaultProfileRepository(
                    FakeAuthRepository(validSessionResult = { Result.success(SAMPLE_SESSION) }),
                    FakeProfileApi(fetchProfileResult = { throw apiFailure }),
                )

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

private class FakeAuthRepository(
    private val validSessionResult: () -> Result<Session> = { error("not stubbed") },
) : AuthRepository {
    override suspend fun currentSession(): Session? = error("not stubbed")

    override suspend fun completeLogin(code: String): Result<Unit> = error("not stubbed")

    override suspend fun refreshSession(): Result<Session> = error("not stubbed")

    override suspend fun validSession(): Result<Session> = validSessionResult()

    override suspend fun logout() = error("not stubbed")
}

private class FakeProfileApi(
    private val fetchProfileResult: () -> Profile = { error("not stubbed") },
) : ProfileApi {
    override suspend fun fetchProfile(accessToken: String): Profile = fetchProfileResult()
}
