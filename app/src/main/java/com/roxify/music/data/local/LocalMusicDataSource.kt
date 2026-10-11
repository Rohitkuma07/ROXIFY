package com.roxify.music.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSource
import com.roxify.music.util.MediaPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads audio from the device using MediaStore. No file paths are hard coded and the
 * cursor is always closed. Returns an empty list (rather than throwing) when the audio
 * permission has not been granted yet, so the UI can show a proper permission state.
 */
class LocalMusicDataSource(private val context: Context) {

    private val collection: Uri
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        if (!MediaPermissions.hasAudioPermission(context)) return@withContext emptyList()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val result = ArrayList<Song>()
        runCatching {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val displayCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val artistIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val display = cursor.stringOrNull(displayCol)
                    val title = cursor.stringOrNull(titleCol)?.takeIf { it.isNotBlank() }
                        ?: display?.substringBeforeLast('.')
                        ?: "Unknown track"
                    val artist = cursor.stringOrNull(artistCol)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: "Unknown artist"
                    val album = cursor.stringOrNull(albumCol)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: "Unknown album"
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    val artistId = if (artistIdCol >= 0) cursor.getLong(artistIdCol) else 0L
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) * 1000L else 0L
                    val track = if (trackCol >= 0) cursor.getInt(trackCol) % 1000 else 0
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                    val path = cursor.stringOrNull(dataCol)
                    val folder = path?.substringBeforeLast('/', "")?.substringAfterLast('/')
                        ?.takeIf { it.isNotBlank() }

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val artwork = albumArtUri(albumId)

                    result += Song(
                        id = "local:$id",
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        artistId = artistId,
                        durationMs = duration,
                        uri = contentUri.toString(),
                        artworkUri = artwork,
                        source = SongSource.LOCAL,
                        folder = folder,
                        dateAdded = dateAdded,
                        trackNumber = track,
                        year = year
                    )
                }
            }
        }
        result
    }

    private fun albumArtUri(albumId: Long): String? {
        if (albumId <= 0L) return null
        return runCatching {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), albumId
            ).toString()
        }.getOrNull()
    }

    private fun android.database.Cursor.stringOrNull(index: Int): String? {
        if (index < 0 || isNull(index)) return null
        return getString(index)
    }
}
