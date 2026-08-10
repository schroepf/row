package de.mistatee.erglog.ui.main

import de.mistatee.erglog.data.profile.Profile
import de.mistatee.erglog.data.profile.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyLoading() =
        runTest(testDispatcher) {
            val profileRepository = mockk<ProfileRepository>()
            coEvery { profileRepository.fetchProfile() } coAnswers { awaitCancellation() }
            val viewModel = MainScreenViewModel(profileRepository)
            assertEquals(MainScreenUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_onFetchSuccess_isDisplayed() =
        runTest(testDispatcher) {
            val profileRepository = mockk<ProfileRepository>()
            coEvery { profileRepository.fetchProfile() } returns Result.success(Profile(username = "Sample"))
            val viewModel = MainScreenViewModel(profileRepository)

            advanceUntilIdle()

            assertEquals(MainScreenUiState.Success("Sample"), viewModel.uiState.value)
        }

    @Test
    fun uiState_onFetchFailure_showsError() =
        runTest(testDispatcher) {
            val failure = IllegalStateException("boom")
            val profileRepository = mockk<ProfileRepository>()
            coEvery { profileRepository.fetchProfile() } returns Result.failure(failure)
            val viewModel = MainScreenViewModel(profileRepository)

            advanceUntilIdle()

            assertEquals(MainScreenUiState.Error(failure), viewModel.uiState.value)
        }
}
