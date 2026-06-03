package com.schroepf.row

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RowReducerTest {
    private val profile = RowProfile(
        username = "davidhart",
        fullName = "David Hart",
        email = "davidh@concept2.com",
        country = "GBR"
    )

    @Test
    fun `loading result enables loading and clears error`() {
        val initial = RowState(isLoading = false, profile = profile, error = "error")

        val result = RowReducer.reduce(initial, RowResult.Loading)

        assertTrue(result.isLoading)
        assertEquals(profile, result.profile)
        assertNull(result.error)
    }

    @Test
    fun `success result stores profile and disables loading`() {
        val initial = RowState(isLoading = true, profile = null, error = "error")

        val result = RowReducer.reduce(initial, RowResult.Success(profile))

        assertFalse(result.isLoading)
        assertEquals(profile, result.profile)
        assertNull(result.error)
    }

    @Test
    fun `failure result stores error clears profile and disables loading`() {
        val initial = RowState(isLoading = true, profile = profile, error = null)

        val result = RowReducer.reduce(initial, RowResult.Failure("network failed"))

        assertFalse(result.isLoading)
        assertNull(result.profile)
        assertEquals("network failed", result.error)
    }
}
