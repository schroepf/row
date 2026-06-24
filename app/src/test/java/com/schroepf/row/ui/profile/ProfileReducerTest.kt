package com.schroepf.row.ui.profile

import com.schroepf.row.api.log.UserProfile
import org.junit.Assert
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

        Assert.assertTrue(result.isLoading)
        Assert.assertEquals(profile, result.profile)
        Assert.assertNull(result.error)
    }

    @Test
    fun `success result stores profile and disables loading`() {
        val initial = ProfileState(isLoading = true, profile = null, error = "error")

        val result = ProfileReducer.reduce(initial, ProfileResult.Success(profile))

        Assert.assertFalse(result.isLoading)
        Assert.assertEquals(profile, result.profile)
        Assert.assertNull(result.error)
    }

    @Test
    fun `failure result stores error clears profile and disables loading`() {
        val initial = ProfileState(isLoading = true, profile = profile, error = null)

        val result = ProfileReducer.reduce(initial, ProfileResult.Failure("network failed"))

        Assert.assertFalse(result.isLoading)
        Assert.assertNull(result.profile)
        Assert.assertEquals("network failed", result.error)
    }
}