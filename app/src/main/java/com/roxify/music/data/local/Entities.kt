package com.roxify.music.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Favourites are stored denormalised (a serialised [com.roxify.music.domain.model.Song])
 * so that the list keeps working even if the underlying MediaStore row changes or the
 * file is temporarily unavailable.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: String,
    val songJson: String,
    val addedAt: Long
)

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val trackId: String,
    val songJson: String,
    val playedAt: Long
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "playlist_songs",
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val playlistId: Long,
    val trackId: String,
    val songJson: String,
    val position: Int,
    val addedAt: Long
)

/** Projection used to attach a song count to each playlist without N+1 queries. */
data class PlaylistSongCount(
    val playlistId: Long,
    val count: Int
)
