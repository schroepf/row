package de.mistatee.erglog.ui.main

import androidx.paging.PagingData
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.mistatee.erglog.common.CoroutineTestRule
import de.mistatee.erglog.data.model.Profile
import de.mistatee.erglog.data.repository.ResultRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val noResults = mockk<ResultRepository> {
        every { observeResults() } returns flowOf(PagingData.empty())
    }

    @Test
    fun `uiState is initially Loading`() = runTest {
        // Given
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                every { observeProfile() } returns flowOf(null)
                coEvery { refresh() } returns Result.success(Unit)
            },
            resultRepository = noResults,
        )

        // When
        val initialUiState = viewModel.uiState.value

        // Then
        assertThat(initialUiState).isEqualTo(MainScreenUiState.Loading)
    }

    @Test
    fun `uiState shows cached username without waiting for refresh to complete`() = runTest {
        // Given
        val username = "Sample"
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                every { observeProfile() } returns flowOf(Profile(username = username))
                coEvery { refresh() } coAnswers { awaitCancellation() }
            },
            resultRepository = noResults,
        )

        // When
        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Success(username = username, syncError = null))
    }

    @Test
    fun `uiState carries a syncError alongside cached data when refresh fails`() = runTest {
        // Given
        val username = "Sample"
        val exception = IllegalStateException("boom")
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                every { observeProfile() } returns flowOf(Profile(username = username))
                coEvery { refresh() } returns Result.failure(exception)
            },
            resultRepository = noResults,
        )

        // When
        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Success(username = username, syncError = exception))
    }

    @Test
    fun `uiState is Error when there is no cached profile and refresh fails`() = runTest {
        // Given
        val exception = IllegalStateException("boom")
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                every { observeProfile() } returns flowOf(null)
                coEvery { refresh() } returns Result.failure(exception)
            },
            resultRepository = noResults,
        )

        // When
        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Error(exception))
    }

    @Test
    fun `uiState stays Loading while there is no cached profile and refresh has not completed`() = runTest {
        // Given
        val viewModel = MainScreenViewModel(
            profileRepository = mockk {
                every { observeProfile() } returns flowOf(null)
                coEvery { refresh() } coAnswers { awaitCancellation() }
            },
            resultRepository = noResults,
        )

        // When
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState).isEqualTo(MainScreenUiState.Loading)
    }
}
