package com.schroepf.row

import com.schroepf.row.api.log.RowApi
import com.schroepf.row.api.log.UserProfile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class RowViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = UserProfile(
        username = "davidhart",
        fullName = "David Hart",
        email = "davidh@concept2.com",
        country = "GBR"
    )

    @Test
    fun `authorization code loads concept2 profile`() = runTest {
        val viewModel = RowViewModel(
            rowApi = object : RowApi {
                override suspend fun fetchUserProfile(authorizationCode: String, codeVerifier: String?): UserProfile {
                    delay(1)
                    assertEquals("auth-code", authorizationCode)
                    return profile
                }
            }
        )

        viewModel.send(RowIntent.AuthorizationCodeReceived("auth-code", codeVerifier = null))

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(profile, viewModel.state.value.profile)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `authorization code failure exposes error`() = runTest {
        val viewModel = RowViewModel(
            rowApi = object : RowApi {
                override suspend fun fetchUserProfile(authorizationCode: String, codeVerifier: String?): UserProfile {
                    throw IllegalStateException("invalid_grant")
                }
            }
        )

        viewModel.send(RowIntent.AuthorizationCodeReceived("bad-code", codeVerifier = null))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.profile)
        assertEquals("invalid_grant", viewModel.state.value.error)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
