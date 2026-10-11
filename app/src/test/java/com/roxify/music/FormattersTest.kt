package com.roxify.music

import com.roxify.music.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure formatter tests (no Android runtime needed). */
class FormattersTest {

    @Test
    fun `duration formats m ss and h mm ss`() {
        assertEquals("0:00", Formatters.duration(0L))
        assertEquals("0:00", Formatters.duration(-10L))
        assertEquals("0:12", Formatters.duration(12_000L))
        assertEquals("3:07", Formatters.duration(187_000L))
        assertEquals("1:02:03", Formatters.duration(3_723_000L))
    }

    @Test
    fun `compact count scales to K and M`() {
        assertEquals("12", Formatters.compactCount(12))
        assertEquals("1.2K", Formatters.compactCount(1_200))
        assertEquals("999.9K", Formatters.compactCount(999_900))
        assertEquals("1.5M", Formatters.compactCount(1_500_000))
    }

    @Test
    fun `listening time renders hours and minutes`() {
        assertEquals("0m", Formatters.listeningTime(0L))
        assertEquals("12m", Formatters.listeningTime(12L))
        assertEquals("3h", Formatters.listeningTime(180L))
        assertEquals("3h 24m", Formatters.listeningTime(204L))
    }

    @Test
    fun `greeting covers all hours`() {
        val seen = (0L..23L).map { Formatters.greeting(it.toInt()) }.toSet()
        assertTrue("Good morning" in seen)
        assertTrue("Good afternoon" in seen)
        assertTrue("Good evening" in seen)
        assertTrue("Good night" in seen)
        // every hour maps to exactly one of the four categories
        assertEquals(4, seen.size)
    }
}