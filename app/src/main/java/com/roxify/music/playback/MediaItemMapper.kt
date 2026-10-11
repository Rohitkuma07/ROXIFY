package com.roxify.music.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.roxify.music.data.mapper.SongJson
import com.roxify.music.domain.model.Song

/**
 * Converts domain [Song]s to Media3 [MediaItem]s and back. The full song is carried in
 * the item's metadata extras so playback state survives process recreation without a
 * second lookup.
 */
object MediaItemMapper {

    const val EXTRA_SONG = "com.roxify.music.EXTRA_SONG"

    fun toMediaItem(song: Song): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album.ifBlank { null })
            .setExtras(Bundle().apply { putString(EXTRA_SONG, SongJson.encode(song)) })

        song.artworkUri?.let { runCatching { metadataBuilder.setArtworkUri(Uri.parse(it)) } }

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    fun toMediaItems(songs: List<Song>): List<MediaItem> = songs.map { toMediaItem(it) }

    fun fromMediaItem(item: MediaItem?): Song? {
        item ?: return null
        val json = item.mediaMetadata.extras?.getString(EXTRA_SONG)
        if (json != null) {
            SongJson.decode(json)?.let { return it }
        }
        return Song(
            id = item.mediaId,
            title = item.mediaMetadata.title?.toString() ?: "Unknown track",
            artist = item.mediaMetadata.artist?.toString() ?: "Unknown artist",
            album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
            uri = item.localConfiguration?.uri?.toString().orEmpty()
        )
    }

    fun fromMediaItems(items: List<MediaItem>): List<Song> = items.mapNotNull { fromMediaItem(it) }
}
