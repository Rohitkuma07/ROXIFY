package com.roxify.music

import com.roxify.music.data.local.DemoCatalog
import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.SongSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ensures the bundled demo catalogue is honest: each entry is DEMO and references a real asset. */
class DemoCatalogTest {

    private val assets = setOf(
        "demo_romantic.wav",
        "demo_sad.wav",
        "demo_chill.wav",
        "demo_party.wav",
        "demo_workout.wav",
        "demo_lofi.wav"
    )

    @Test
    fun `every demo track is tagged as demo`() {
        assertTrue(DemoCatalog.songs.isNotEmpty())
        DemoCatalog.songs.forEach { song ->
            assertEquals(SongSource.DEMO, song.source)
            assertTrue("demo ${song.id} must carry a demo uri", song.uri.startsWith("asset:///demo/"))
        }
    }

    @Test
    fun `every demo track maps to one of the bundled assets`() {
        DemoCatalog.songs.forEach { song ->
            val file = song.uri.removePrefix("asset:///demo/")
            assertTrue("unexpected asset $file", file in assets)
        }
    }

    @Test
    fun `songsForMood returns only matching tracks`() {
        val romantic = DemoCatalog.songsForMood(Mood.ROMANTIC)
        assertTrue(romantic.isNotEmpty())
        romantic.forEach { assertEquals(Mood.ROMANTIC.label, it.genre) }
        assertTrue(DemoCatalog.songsForMood(Mood.LOFI).isNotEmpty())
    }

    @Test
    fun `ids are stable and unique`() {
        val ids = DemoCatalog.songs.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.all { it.startsWith("demo:") })
    }
}