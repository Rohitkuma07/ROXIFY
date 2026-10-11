package com.roxify.music.data.mapper

import com.roxify.music.domain.model.Song
import kotlinx.serialization.json.Json

/**
 * Serialises [Song] into the JSON stored in Room, and back again.
 *
 * Kept in one place so the storage format is versioned/documented and easy to unit test.
 */
object SongJson {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encode(song: Song): String = json.encodeToString(Song.serializer(), song)

    fun decode(value: String): Song? = runCatching {
        json.decodeFromString(Song.serializer(), value)
    }.getOrNull()

    fun encodeList(songs: List<Song>): String = json.encodeToString(
        kotlinx.serialization.builtins.ListSerializer(Song.serializer()), songs
    )

    fun decodeList(value: String): List<Song> = runCatching {
        json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Song.serializer()), value
        )
    }.getOrDefault(emptyList())
}
