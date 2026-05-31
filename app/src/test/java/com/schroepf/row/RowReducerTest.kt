package com.schroepf.row

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RowReducerTest {
    @Test
    fun `loading result enables loading and clears error`() {
        val initial = RowState(isLoading = false, message = "old", error = "error")

        val result = RowReducer.reduce(initial, RowResult.Loading)

        assertTrue(result.isLoading)
        assertEquals("old", result.message)
        assertNull(result.error)
    }

    @Test
    fun `success result stores message and disables loading`() {
        val initial = RowState(isLoading = true, message = "", error = "error")

        val result = RowReducer.reduce(initial, RowResult.Success("new message"))

        assertFalse(result.isLoading)
        assertEquals("new message", result.message)
        assertNull(result.error)
    }

    @Test
    fun `failure result stores error and disables loading`() {
        val initial = RowState(isLoading = true, message = "existing", error = null)

        val result = RowReducer.reduce(initial, RowResult.Failure("network failed"))

        assertFalse(result.isLoading)
        assertEquals("existing", result.message)
        assertEquals("network failed", result.error)
    }
}
