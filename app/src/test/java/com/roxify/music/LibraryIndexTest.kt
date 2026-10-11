package com.roxify.music

import com.roxify.music.domain.LibraryIndex
import com.roxify.music.domain.model.RepeatMode
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSort
import com.roxify.music.domain.model.SongSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure logic tests for the library index: grouping, sorting and search. */
class LibraryIndexTest {

    private fun song(id: String, title: String, artist: String, album: String, added: Long = 0L): Song =
        Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = 1000L,
            uri = "content://$id",
            dateAdded = added,
            trackNumber = 0
        )

    private val library = listOf(
        song("1", "Aaa", "Alpha", "One"),
        song("2", "Bbb", "Alpha", "One"),
        song("3", "Ccc", "Beta", "Two"),
        song("4", "Ddd", "Beta", "Two"),
        song("5", "Eee", "Gamma", "One")
    )

    @Test
    fun `albums group by title and artist`() {
        val albums = LibraryIndex.albums(library)
        // "One"/Alpha and "One"/Gamma are distinct albums (title + artist identity)
        assertEquals(3, albums.size)
        val alphaOne = albums.first { it.title == "One" && it.artist == "Alpha" }
        assertEquals(2, alphaOne.songCount)
        val gammaOne = albums.first { it.title == "One" && it.artist == "Gamma" }
        assertEquals(1, gammaOne.songCount)
        assertTrue(albums.all { it.source == SongSource.LOCAL })
    }

    @Test
    fun `albums are sorted by title`() {
        val albums = LibraryIndex.albums(library)
        assertEquals(listOf("One", "One", "Two"), albums.map { it.title })
        // same-title albums are ordered by artist
        assertEquals(
            listOf("Alpha", "Gamma"),
            albums.filter { it.title == "One" }.map { it.artist }
        )
    }

    @Test
    fun `artists group songs and count albums`() {
        val artists = LibraryIndex.artists(library)
        assertEquals(3, artists.size)
        val alpha = artists.first { it.name == "Alpha" }
        assertEquals(2, alpha.songCount)
        assertEquals(1, alpha.albumCount)
    }

    @Test
    fun `folders group by relative folder path`() {
        val withFolders = library.map {
            it.copy(folder = if (it.album == "One") "A" else "B")
        }
        val folders = LibraryIndex.folders(withFolders)
        assertEquals(2, folders.size)
        assertEquals(3, folders.first { it.name == "A" }.songCount)
    }

    @Test
    fun `sort by title artist album duration and date`() {
        val ascii = library.map {
            it.copy(title = it.title.lowercase(), artist = it.artist.lowercase(), album = it.album.lowercase())
        }
        assertEquals(
            listOf("aaa", "bbb", "ccc", "ddd", "eee"),
            LibraryIndex.sort(ascii, SongSort.TITLE).map { it.title }
        )
        val byArtist = LibraryIndex.sort(ascii, SongSort.ARTIST)
        assertTrue(byArtist.zipWithNext().all { (a, b) -> a.artist <= b.artist })
        val byAlbum = LibraryIndex.sort(ascii, SongSort.ALBUM)
        assertTrue(byAlbum.first { it.album == "one" } != null)
        assertEquals(library.size, LibraryIndex.sort(library, SongSort.DURATION).size)
        // longer sorted before shorter when sorted by duration
        val durations = LibraryIndex.sort(
            listOf(song("a", "x", "y", "z"), song("b", "x", "y", "z").copy(durationMs = 5000L)),
            SongSort.DURATION
        )
        assertEquals(5000L, durations.first().durationMs)
    }

    @Test
    fun `sort by date added newest first`() {
        val songs = listOf(
            song("a", "old", "x", "y", added = 100L),
            song("b", "new", "x", "y", added = 200L)
        )
        val sorted = LibraryIndex.sort(songs, SongSort.DATE_ADDED)
        assertEquals("new", sorted.first().title)
    }

    @Test
    fun `song matches across title artist album and genre`() {
        val s = song("1", "Midnight", "Velvet Club", "Neon", 0L).copy(genre = "Chill")
        assertTrue(LibraryIndex.songMatches(s, "midnight"))
        assertTrue(LibraryIndex.songMatches(s, "VELVET"))
        assertTrue(LibraryIndex.songMatches(s, "neon"))
        assertTrue(LibraryIndex.songMatches(s, "chill"))
        assertFalse(LibraryIndex.songMatches(s, "rock"))
        assertTrue(LibraryIndex.songMatches(s, " "))
    }

    @Test
    fun `album key uses unit separator and never NUL`() {
        val key = LibraryIndex.albumKey("Greatest Hits", "Artist")
        assertEquals("Greatest Hits\u001FArtist", key)
        assertFalse(key.contains('\u0000'))
        assertEquals("Greatest Hits\u001FArtist", LibraryIndex.albumKey("Greatest Hits", "Artist"))
    }

    @Test
    fun `repeat mode mapping matches media3 conventions`() {
        assertEquals(RepeatMode.ALL, RepeatMode.fromInt(1))
        assertEquals(RepeatMode.ONE, RepeatMode.fromInt(2))
        assertEquals(RepeatMode.OFF, RepeatMode.fromInt(0))
        assertEquals(RepeatMode.OFF, RepeatMode.fromInt(99))
        assertEquals(RepeatMode.OFF, RepeatMode.fromInt(3))
    }

    @Test
    fun `repeat mode cycles off to all to one`() {
        assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
        assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
        assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())
    }
}