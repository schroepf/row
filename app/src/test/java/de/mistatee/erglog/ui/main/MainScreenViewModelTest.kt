package de.mistatee.erglog.ui.main

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.mistatee.erglog.common.CoroutineTestRule
import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Test
    fun `uiState is initially Loading`() = runTest {
        // Given
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                coEvery { fetchProfile() } coAnswers { awaitCancellation() }
            },
            resultRepository = mockk {
                coEvery { fetchPage(any(), any()) } coAnswers { awaitCancellation() }
            },
        )

        // When
        val initialUiState = viewModel.uiState.value

        // Then
        assertThat(initialUiState).isEqualTo(MainScreenUiState.Loading)
    }

    @Test
    fun `uiState has correct username on success`() = runTest {
        // Given
        val username = "Sample"
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                coEvery { fetchProfile() } returns Result.success(Profile(username = username))
            },
            resultRepository = mockk {
                coEvery { fetchPage(any(), any()) } coAnswers { awaitCancellation() }
            },
        )

        // When
        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Success(username = username))
    }

    @Test
    fun `uiState propagates profile error`() = runTest {
        // Given
        val exception = IllegalStateException("boom")
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                coEvery { fetchProfile() } returns Result.failure(exception)
            },
            resultRepository = mockk {
                coEvery { fetchPage(any(), any()) } coAnswers { awaitCancellation() }
            },
        )

        // When
        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Error(exception))
    }
}
