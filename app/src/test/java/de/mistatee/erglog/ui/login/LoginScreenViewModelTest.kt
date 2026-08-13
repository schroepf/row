package de.mistatee.erglog.ui.login

import de.mistatee.erglog.common.CoroutineTestRule
import de.mistatee.erglog.data.concept2.auth.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenViewModelTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Test
    fun `uiState is initially Idle`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(mockk<AuthRepository>())

            // When
            val initialUiState = viewModel.uiState.value

            // Then
            assertEquals(LoginUiState.Idle, initialUiState)
        }

    @Test
    fun `effect is OpenAuthorizationTab with valid url on action LoginClicked`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(
                authRepository = mockk {
                    every { authorizationUrl } returns "https://www.test.com"
                },
            )

            // When
            viewModel.onAction(LoginAction.LoginClicked)
            val effect = viewModel.effects.first()

            // Then
            assertTrue(effect is LoginEffect.OpenAuthorizationTab)
            assertTrue((effect as LoginEffect.OpenAuthorizationTab).url.isNotBlank())
        }

    @Test
    fun `uiState is LoggedIn when AuthorizationResultReceived is valid`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(
                authRepository = mockk {
                    coEvery { completeLogin("auth-code") } returns Result.success(Unit)
                },
            )

            // When
            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = "auth-code", error = null))
            advanceUntilIdle()
            val uiState = viewModel.uiState.value

            // Then
            assertEquals(LoginUiState.LoggedIn, uiState)
        }

    @Test
    fun `uiState is Error when AuthorizationResultReceived contains error`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(mockk())

            // When
            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = "access_denied"))

            // Then
            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun `uiState is Error on empty AuthorizationResultReceived`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(mockk())

            // When
            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = null))

            // Then
            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }

    @Test
    fun `uiState resets to Idle on action RetryClicked`() =
        runTest {
            // Given
            val viewModel = LoginScreenViewModel(mockk())
            viewModel.onAction(LoginAction.AuthorizationResultReceived(code = null, error = "access_denied"))

            // When
            viewModel.onAction(LoginAction.RetryClicked)

            // Then
            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }
}
