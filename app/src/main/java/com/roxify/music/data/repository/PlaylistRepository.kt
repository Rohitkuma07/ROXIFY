package com.roxify.music.data.repository

import com.roxify.music.data.local.PlaylistDao
import com.roxify.music.data.local.PlaylistEntity
import com.roxify.music.data.local.PlaylistSongEntity
import com.roxify.music.data.mapper.SongJson
import com.roxify.music.domain.model.Playlist
import com.roxify.music.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * CRUD for user playlists. Songs are stored denormalised so a playlist survives a
 * MediaStore change, and playlist/song rows are kept consistent by always touching the
 * playlist's `updatedAt` when its contents change.
 */
class PlaylistRepository(private val dao: PlaylistDao) {

    val playlists: Flow<List<Playlist>> =
        combine(dao.observePlaylists(), dao.observeCounts()) { playlists, counts ->
            val countMap = counts.associate { it.playlistId to it.count }
            playlists.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songCount = countMap[entity.id] ?: 0,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }

    val playlistCount: Flow<Int> = dao.observePlaylistCount()
    val totalSongsAcrossPlaylists: Flow<Int> = dao.observeTotalSongCount()

    fun observePlaylist(id: Long): Flow<Playlist?> =
        combine(dao.observePlaylist(id), dao.observeCounts()) { entity, counts ->
            entity?.let {
                Playlist(
                    id = it.id,
                    name = it.name,
                    songCount = counts.firstOrNull { c -> c.playlistId == id }?.count ?: 0,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    fun observeSongs(id: Long): Flow<List<Song>> =
        dao.observeSongs(id).map { list -> list.mapNotNull { SongJson.decode(it.songJson) } }

    fun observeEntries(id: Long): Flow<List<PlaylistSongEntity>> = dao.observeSongs(id)

    suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        return dao.insertPlaylist(
            PlaylistEntity(name = name.trim().ifBlank { "New playlist" }, createdAt = now, updatedAt = now)
        )
    }

    suspend fun renamePlaylist(id: Long, name: String) {
        dao.renamePlaylist(id, name.trim().ifBlank { "Playlist" }, System.currentTimeMillis())
    }

    suspend fun deletePlaylist(id: Long) {
        dao.clearPlaylistSongs(id)
        dao.deletePlaylist(id)
    }

    /** @return true when the song was added, false when it was already in the playlist. */
    suspend fun addSong(playlistId: Long, song: Song): Boolean {
        if (dao.contains(playlistId, song.id)) return false
        val nextPosition = (dao.getSongs(playlistId).maxOfOrNull { it.position } ?: -1) + 1
        dao.insertSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                trackId = song.id,
                songJson = SongJson.encode(song),
                position = nextPosition,
                addedAt = System.currentTimeMillis()
            )
        )
        dao.touchPlaylist(playlistId, System.currentTimeMillis())
        return true
    }

    /** @return the number of songs actually added (duplicates are skipped). */
    suspend fun addSongs(playlistId: Long, songs: List<Song>): Int {
        var added = 0
        songs.forEach { if (addSong(playlistId, it)) added++ }
        return added
    }

    suspend fun removeSong(playlistId: Long, trackId: String) {
        dao.removeSong(playlistId, trackId)
        dao.touchPlaylist(playlistId, System.currentTimeMillis())
    }

    suspend fun removeEntry(entryId: Long, playlistId: Long) {
        dao.removeSongByEntryId(entryId)
        dao.touchPlaylist(playlistId, System.currentTimeMillis())
    }

    /** Moves an entry by one slot in the given direction (used by drag handles). */
    suspend fun moveEntry(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val entries = dao.getSongs(playlistId)
        if (fromIndex !in entries.indices || toIndex !in entries.indices) return
        val reordered = entries.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        reordered.forEachIndexed { index, entry -> dao.updatePosition(entry.id, index) }
        dao.touchPlaylist(playlistId, System.currentTimeMillis())
    }

    suspend fun contains(playlistId: Long, trackId: String): Boolean = dao.contains(playlistId, trackId)
}
