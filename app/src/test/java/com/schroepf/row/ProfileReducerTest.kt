package com.schroepf.row

import com.schroepf.row.api.log.UserProfile
import com.schroepf.row.ui.profile.ProfileReducer
import com.schroepf.row.ui.profile.ProfileResult
import com.schroepf.row.ui.profile.ProfileState

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileReducerTest {
    private val profile = UserProfile(
        username = "davidhart",
        fullName = "David Hart",
        email = "davidh@concept2.com",
        country = "GBR"
    )

    @Test
    fun `loading result enables loading and clears error`() {
        val initial = ProfileState(isLoading = false, profile = profile, error = "error")

        val result = ProfileReducer.reduce(initial, ProfileResult.Loading)

        assertTrue(result.isLoading)
        assertEquals(profile, result.profile)
        assertNull(result.error)
    }

    @Test
    fun `success result stores profile and disables loading`() {
        val initial = ProfileState(isLoading = true, profile = null, error = "error")

        val result = ProfileReducer.reduce(initial, ProfileResult.Success(profile))

        assertFalse(result.isLoading)
        assertEquals(profile, result.profile)
        assertNull(result.error)
    }

    @Test
    fun `failure result stores error clears profile and disables loading`() {
        val initial = ProfileState(isLoading = true, profile = profile, error = null)

        val result = ProfileReducer.reduce(initial, ProfileResult.Failure("network failed"))

        assertFalse(result.isLoading)
        assertNull(result.profile)
        assertEquals("network failed", result.error)
    }
}
