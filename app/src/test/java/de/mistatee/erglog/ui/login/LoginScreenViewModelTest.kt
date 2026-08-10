package de.mistatee.erglog.ui.login

import de.mistatee.erglog.data.auth.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
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
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())
            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }

    @Test
    fun onAction_loginClicked_emitsOpenAuthorizationTabEffect() =
        runTest {
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())

            viewModel.onAction(LoginAction.LoginClicked)
            val effect = viewModel.effects.first()

            assertTrue(effect is LoginEffect.OpenAuthorizationTab)
            assertTrue((effect as LoginEffect.OpenAuthorizationTab).url.isNotBlank())
        }

    @Test
    fun onAuthorizationResult_withCode_logsIn() =
        runTest {
            val authRepository = mockk<AuthRepository>()
            coEvery { authRepository.completeLogin("auth-code") } returns Result.success(Unit)
            val viewModel = LoginScreenViewModel(authRepository)

            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = "auth-code", error = null))
            advanceUntilIdle()

            assertEquals(LoginUiState.LoggedIn, viewModel.uiState.value)
        }

    @Test
    fun onAuthorizationResult_withError_showsError() =
        runTest {
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())

            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = "access_denied"))

            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun onAuthorizationResult_withoutCodeOrError_showsError() =
        runTest {
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())

            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = null))

            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun onRetry_resetsToIdle() =
        runTest {
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())
            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = "access_denied"))

            viewModel.onAction(LoginAction.RetryClicked)

            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }
}
