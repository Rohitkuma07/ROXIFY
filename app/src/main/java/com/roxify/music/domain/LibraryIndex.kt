package com.roxify.music.domain

import com.roxify.music.domain.model.Album
import com.roxify.music.domain.model.Artist
import com.roxify.music.domain.model.MusicFolder
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSort

/**
 * Pure functions that turn a flat list of [Song] into albums, artists, folders and search
 * hits. Kept free of Android APIs so it can be unit tested on the JVM.
 */
object LibraryIndex {

    fun albums(songs: List<Song>): List<Album> =
        songs.filter { it.album.isNotBlank() }
            .groupBy { it.album to it.artist }
            .map { (key, group) ->
                Album(
                    id = albumKey(key.first, key.second),
                    title = key.first,
                    artist = key.second,
                    artworkUri = group.firstNotNullOfOrNull { it.artworkUri },
                    songCount = group.size,
                    year = group.firstOrNull { it.year > 0 }?.year ?: 0,
                    source = group.first().source
                )
            }
            .sortedBy { it.title.lowercase() }

    fun artists(songs: List<Song>): List<Artist> =
        songs.filter { it.artist.isNotBlank() }
            .groupBy { it.artist }
            .map { (name, group) ->
                Artist(
                    id = artistKey(name),
                    name = name,
                    artworkUri = group.firstNotNullOfOrNull { it.artworkUri },
                    songCount = group.size,
                    albumCount = group.map { it.album }.filter { it.isNotBlank() }.distinct().size,
                    source = group.first().source
                )
            }
            .sortedBy { it.name.lowercase() }

    fun folders(songs: List<Song>): List<MusicFolder> =
        songs.filter { !it.folder.isNullOrBlank() }
            .groupBy { it.folder!! }
            .map { (folder, group) ->
                MusicFolder(id = folder, name = folder, songCount = group.size)
            }
            .sortedBy { it.name.lowercase() }

    fun songMatches(song: Song, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return song.title.lowercase().contains(q) ||
            song.artist.lowercase().contains(q) ||
            song.album.lowercase().contains(q) ||
            song.genre?.lowercase()?.contains(q) == true
    }

    fun sort(songs: List<Song>, sort: SongSort): List<Song> = when (sort) {
        SongSort.TITLE -> songs.sortedBy { it.title.lowercase() }
        SongSort.ARTIST -> songs.sortedWith(
            compareBy({ it.artist.lowercase() }, { it.title.lowercase() })
        )
        SongSort.ALBUM -> songs.sortedWith(
            compareBy({ it.album.lowercase() }, { it.trackNumber }, { it.title.lowercase() })
        )
        SongSort.DURATION -> songs.sortedByDescending { it.durationMs }
        SongSort.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
    }

    /** Stable, URL-safe album key ("title<US>artist") that never contains a NUL byte. */
    fun albumKey(title: String, artist: String): String = "$title\u001F$artist"
    fun artistKey(name: String): String = name
}
