package com.roxify.music

import com.roxify.music.data.mapper.SongJson
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Round-trip serialisation of the denormalised song JSON stored in Room. */
class SongJsonTest {

    private val song = Song(
        id = "demo:3",
        title = "Rain On Glass (Demo)",
        artist = "ROXIFY Studio",
        album = "Neon Sessions (Demo)",
        durationMs = 16_000L,
        uri = "asset:///demo/demo_sad.wav",
        artworkUri = null,
        source = SongSource.DEMO,
        genre = "Sad",
        folder = null,
        dateAdded = 123L,
        trackNumber = 3,
        year = 2026
    )

    @Test
    fun `song round trips through json`() {
        val decoded = SongJson.decode(SongJson.encode(song))
        assertNotNull(decoded)
        assertEquals(song, decoded)
    }

    @Test
    fun `list round trips through json`() {
        val list = listOf(song, song.copy(id = "local:9", title = "Device track", source = SongSource.LOCAL))
        val decoded = SongJson.decodeList(SongJson.encodeList(list))
        assertEquals(2, decoded.size)
        assertEquals(list, decoded)
    }

    @Test
    fun `decode tolerates unknown keys and bad input`() {
        val withExtra = """{"id":"x","title":"t","artist":"a","extraField":42}"""
        val decoded = SongJson.decode(withExtra)
        assertEquals("x", decoded?.id)
        assertNull(SongJson.decode("not json at all"))
    }

    @Test
    fun `decode defaults optional fields`() {
        val compact = """{"id":"x","title":"t","artist":"a"}"""
        val decoded = SongJson.decode(compact)
        assertEquals(SongSource.LOCAL, decoded?.source)
        assertEquals("", decoded?.album)
        assertEquals(0L, decoded?.durationMs)
    }
}