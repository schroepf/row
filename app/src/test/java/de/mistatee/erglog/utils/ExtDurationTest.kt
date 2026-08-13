package de.mistatee.erglog.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ExtDurationTest {
    @Test
    fun `formatResultDuration for less then a minute`() {
        // given
        val duration = 45.seconds

        // when
        val formatted = duration.formatResultDuration()

        // then
        assertEquals("0:45", formatted)
    }

    @Test
    fun `formatResultDuration for less then an hour`() {
        // given
        val duration = 13.minutes + 55.seconds

        // when
        val formatted = duration.formatResultDuration()

        // then
        assertEquals("13:55", formatted)
    }

    @Test
    fun `formatResultDuration for more then one hour`() {
        // given
        val duration = 4.hours + 13.minutes + 55.seconds

        // when
        val formatted = duration.formatResultDuration()

        // then
        assertEquals("4:13:55", formatted)
    }

    @Test
    fun `formatResultDuration pads single digits with zeros`() {
        // given
        val duration = 1.hours + 2.minutes + 3.seconds

        // when
        val formatted = duration.formatResultDuration()

        // then
        assertEquals("1:02:03", formatted)
    }

    @Test
    fun `formatResultDuration for 0 seconds`() {
        // given
        val duration = 0.seconds

        // when
        val formatted = duration.formatResultDuration()

        // then
        assertEquals("0:00", formatted)
    }
}
