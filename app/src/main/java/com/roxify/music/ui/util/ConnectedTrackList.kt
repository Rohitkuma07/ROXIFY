package com.roxify.music.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.Song
import com.roxify.music.ui.components.TrackListBody
import com.roxify.music.ui.navigation.Routes
import kotlinx.coroutines.launch

/**
 * Wires the shared [TrackListBody] to the app container: playback, favourite toggling and
 * the track action sheet. Every list screen uses this so behaviour is identical.
 */
@Composable
fun ConnectedTrackList(
    container: AppContainer,
    nav: NavController,
    songs: List<Song>,
    favoriteIds: Set<String>,
    modifier: Modifier = Modifier,
    openPlayerOnPlay: Boolean = true,
    removeLabel: String? = null,
    onRemoveSong: ((Song) -> Unit)? = null
) {
    val playerState by container.playbackManager.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var actionSong by remember { mutableStateOf<Song?>(null) }

    TrackListBody(
        songs = songs,
        favoriteIds = favoriteIds,
        currentSongId = playerState.currentSong?.id,
        isPlaying = playerState.isPlaying,
        onPlay = { list, index ->
            container.playbackManager.playSongs(list, index)
            if (openPlayerOnPlay) nav.navigate(Routes.PLAYER)
        },
        onToggleFavorite = { song -> scope.launch { container.musicRepository.toggleFavorite(song) } },
        onMore = { actionSong = it },
        modifier = modifier
    )

    SongSheetHost(
        container = container,
        song = actionSong,
        nav = nav,
        onDismiss = { actionSong = null },
        removeLabel = removeLabel ?: "Remove from list",
        onRemove = onRemoveSong
    )
}
