package com.roxify.music.domain.model

import kotlinx.serialization.Serializable

/** Where a track came from. Used for labelling and for deciding behaviour. */
@Serializable
enum class SongSource {
    /** Scanned from the user's device via MediaStore. */
    LOCAL,

    /** Bundled, clearly-labelled demo audio (royalty-free tones generated for ROXIFY). */
    DEMO,

    /** Provided by an authorised online catalogue integration. */
    ONLINE
}

/**
 * A playable track. The [id] is a stable string key so that local MediaStore ids,
 * demo ids and online ids never collide.
 */
@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val albumId: Long = 0L,
    val artistId: Long = 0L,
    val durationMs: Long = 0L,
    val uri: String = "",
    val artworkUri: String? = null,
    val source: SongSource = SongSource.LOCAL,
    val genre: String? = null,
    val folder: String? = null,
    val dateAdded: Long = 0L,
    val trackNumber: Int = 0,
    val year: Int = 0
) {
    val isDemo: Boolean get() = source == SongSource.DEMO
    val isLocal: Boolean get() = source == SongSource.LOCAL
    val isOnline: Boolean get() = source == SongSource.ONLINE
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: String?,
    val songCount: Int,
    val year: Int = 0,
    val source: SongSource = SongSource.LOCAL
)

data class Artist(
    val id: String,
    val name: String,
    val artworkUri: String?,
    val songCount: Int,
    val albumCount: Int,
    val source: SongSource = SongSource.LOCAL
)

data class MusicFolder(
    val id: String,
    val name: String,
    val songCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val coverUri: String? = null
) {
    val isSystem: Boolean get() = id < 0
}

/** Mood based groupings used across Home / Search. */
enum class Mood(val id: String, val label: String) {
    ROMANTIC("romantic", "Romantic"),
    SAD("sad", "Sad"),
    CHILL("chill", "Chill"),
    PARTY("party", "Party"),
    WORKOUT("workout", "Workout"),
    LOFI("lofi", "Lo-fi");

    companion object {
        fun fromId(id: String?): Mood? = entries.firstOrNull { it.id.equals(id, true) }
    }
}

/** App level repeat mode, decoupled from Media3 so the domain stays framework free. */
enum class RepeatMode {
    OFF, ONE, ALL;

    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }

    companion object {
        fun fromInt(value: Int): RepeatMode = when (value) {
            1 -> ALL
            2 -> ONE
            else -> OFF
        }
    }
}

data class SearchResults(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()

    val totalCount: Int
        get() = songs.size + albums.size + artists.size + playlists.size
}

/** Sort options for library / local music screens. */
enum class SongSort(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Recently added")
}
