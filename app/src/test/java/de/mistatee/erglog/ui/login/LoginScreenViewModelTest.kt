package de.mistatee.erglog.ui.login

import de.mistatee.erglog.data.auth.AuthRepository
import de.mistatee.erglog.data.auth.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginScreenViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyIdle() =
        runTest {
            val viewModel = LoginScreenViewModel(FakeAuthRepository())
            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }

    @Test
    fun onAuthorizationResult_withCode_logsIn() =
        runTest {
            val viewModel = LoginScreenViewModel(FakeAuthRepository(completeLoginResult = { Result.success(Unit) }))

            viewModel.onAuthorizationResult(code = "auth-code", error = null)
            advanceUntilIdle()

            assertEquals(LoginUiState.LoggedIn, viewModel.uiState.value)
        }

    @Test
    fun onAuthorizationResult_withError_showsError() =
        runTest {
            val viewModel = LoginScreenViewModel(FakeAuthRepository())

            viewModel.onAuthorizationResult(code = null, error = "access_denied")

            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun onAuthorizationResult_withoutCodeOrError_showsError() =
        runTest {
            val viewModel = LoginScreenViewModel(FakeAuthRepository())

            viewModel.onAuthorizationResult(code = null, error = null)

            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun onRetry_resetsToIdle() =
        runTest {
            val viewModel = LoginScreenViewModel(FakeAuthRepository())
            viewModel.onAuthorizationResult(code = null, error = "access_denied")

            viewModel.onRetry()

            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }
}

private class FakeAuthRepository(
    private val completeLoginResult: () -> Result<Unit> = { error("not stubbed") },
) : AuthRepository {
    override suspend fun currentSession(): Session? = null

    override suspend fun completeLogin(code: String): Result<Unit> = completeLoginResult()

    override suspend fun refreshSession(): Result<Session> = error("not stubbed")

    override suspend fun logout() = Unit
}
