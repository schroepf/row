package de.mistatee.erglog.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtInstantTest {
    @Test
    fun `formatResultDate returns capitalized month name, day and year`() {
        // given
        val instant = LocalDateTime(2013, 6, 21, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())

        // when
        val formatted = instant.formatResultDate()

        // then
        assertEquals("June 21, 2013", formatted)
    }
}
