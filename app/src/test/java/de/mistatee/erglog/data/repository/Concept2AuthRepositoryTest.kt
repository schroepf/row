package de.mistatee.erglog.data.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.local.LocalCache
import de.mistatee.erglog.data.local.PreferencesStore
import de.mistatee.erglog.data.model.SessionError
import de.mistatee.erglog.data.remote.auth.RemoteAuthDataSource
import de.mistatee.erglog.data.remote.auth.model.RemoteAuthException
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class Concept2AuthRepositoryTest {
    private val localCache = mockk<LocalCache>(relaxUnitFun = true)

    @Test
    fun `completeLogin saves session on success`() = runTest {
        // given
        val authCode = "auth-code"
        val tokenResponse = MockData.Auth.remoteTokenResponse
        val preferencesStore = mockk<PreferencesStore> {
            coEvery { saveSession(any()) } just Runs
            coEvery { getSession() } just Awaits
        }

        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk {
                coEvery { exchangeAuthorizationCode(authCode) } returns tokenResponse
            },
            preferencesStore = preferencesStore,
            clock = MockData.clock,
            localCache = localCache,
        )

        // when
        val result = repository.completeLogin(authCode)

        // then
        assertThat(result.isSuccess).isTrue()
        coVerify {
            preferencesStore.saveSession(
                withArg {
                    assertThat(it.accessToken).isEqualTo(tokenResponse.accessToken)
                    assertThat(it.refreshToken).isEqualTo(tokenResponse.refreshToken)
                },
            )
        }
    }

    @Test
    fun `completeLogin does not save session on failure`() = runTest {
        // Given
        val authCode = "bad-code"
        val preferencesStore = mockk<PreferencesStore>()

        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk {
                coEvery { exchangeAuthorizationCode(authCode) } throws RemoteAuthException(
                    "invalid_grant",
                    null,
                )
            },
            preferencesStore = preferencesStore,
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.completeLogin(authCode)

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { preferencesStore.saveSession(any()) }
    }

    @Test
    fun `refreshSession clears session on authentication failure`() = runTest {
        // Given
        val preferencesStore = mockk<PreferencesStore> {
            coEvery { clearSession() } just Runs
            coEvery { getSession() } returns MockData.Auth.session
        }

        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk<RemoteAuthDataSource> {
                coEvery { refreshAccessToken(any()) } throws RemoteAuthException("invalid_grant", null)
            },
            preferencesStore = preferencesStore,
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.refreshSession()

        // Then
        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 1) { preferencesStore.clearSession() }
    }

    @Test
    fun `currentSession returns stored session`() = runTest {
        // Given
        val session = MockData.Auth.session

        // When
        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk(),
            preferencesStore = mockk {
                coEvery { getSession() } returns session
            },
            clock = MockData.clock,
            localCache = localCache,
        )

        // Then
        assertThat(repository.currentSession()).isEqualTo(session)
    }

    @Test
    fun `validSession returns existing session if session is fresh`() = runTest {
        // Given
        val freshSession =
            MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().plus(3600.seconds))
        val remoteAuthDataSource = mockk<RemoteAuthDataSource>()
        val repository = Concept2AuthRepository(
            remoteAuthDataSource = remoteAuthDataSource,
            preferencesStore = mockk {
                coEvery { getSession() } returns freshSession
            },
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.validSession()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(freshSession)
        coVerify(exactly = 0) { remoteAuthDataSource.refreshAccessToken(any()) }
    }

    @Test
    fun `validSession returns a fresh session when session is stale`() = runTest {
        // Given
        val staleSession =
            MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().minus(1.seconds))
        val tokenResponse = MockData.Auth.remoteTokenResponse

        val preferencesStore = mockk<PreferencesStore> {
            coEvery { getSession() } returns staleSession
            coEvery { saveSession(any()) } just Runs
        }
        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk {
                coEvery { refreshAccessToken(any()) } returns tokenResponse
            },
            preferencesStore = preferencesStore,
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.validSession()

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.accessToken).isEqualTo(tokenResponse.accessToken)
    }

    @Test
    fun `validSession returns NoSession error on missing session`() = runTest {
        // Given
        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk(),
            preferencesStore = mockk {
                coEvery { getSession() } returns null
            },
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.validSession()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<SessionError.NoSession>()
    }

    @Test
    fun `validSession returns RefreshFailed error when refresh fails`() = runTest {
        // Given
        val staleSession =
            MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().minus(1.seconds))
        val cause = RemoteAuthException("invalid_grant", null)

        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk {
                coEvery { refreshAccessToken(any()) } throws cause
            },
            preferencesStore = mockk {
                coEvery { getSession() } returns staleSession
                coEvery { clearSession() } returns Unit
            },
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        val result = repository.validSession()

        // Then
        assertThat(result.isFailure).isTrue()
        val error = result.exceptionOrNull()
        assertThat(error).isNotNull().isInstanceOf<SessionError.RefreshFailed>()
        assertThat(error?.cause).isEqualTo(cause)
    }

    @Test
    fun `logout clears the session and wipes the local cache`() = runTest {
        // Given
        val preferencesStore = mockk<PreferencesStore> {
            coEvery { clearSession() } just Runs
        }
        val repository = Concept2AuthRepository(
            remoteAuthDataSource = mockk(),
            preferencesStore = preferencesStore,
            clock = MockData.clock,
            localCache = localCache,
        )

        // When
        repository.logout()

        // Then
        coVerify(exactly = 1) { preferencesStore.clearSession() }
        coVerify(exactly = 1) { localCache.clear() }
    }
}
