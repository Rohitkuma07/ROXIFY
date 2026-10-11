package com.roxify.music.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.roxify.music.domain.model.Song

/**
 * Presentational track list shared by every "list of songs" screen: album, artist,
 * playlist, favourites, recently played and local music.
 */
@Composable
fun TrackListBody(
    songs: List<Song>,
    favoriteIds: Set<String>,
    currentSongId: String?,
    isPlaying: Boolean,
    onPlay: (List<Song>, Int) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onMore: (Song) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 8.dp, end = 8.dp, top = 4.dp, bottom = 140.dp
    )
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                isCurrent = currentSongId == song.id,
                isPlaying = currentSongId == song.id && isPlaying,
                isFavorite = song.id in favoriteIds,
                onClick = { onPlay(songs, index) },
                onToggleFavorite = { onToggleFavorite(song) },
                onMore = { onMore(song) }
            )
        }
    }
}

/** Standard empty state used by track list screens. */
@Composable
fun TrackListEmpty(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.MusicOff
) {
    EmptyState(icon = icon, title = title, message = message, modifier = modifier)
}
