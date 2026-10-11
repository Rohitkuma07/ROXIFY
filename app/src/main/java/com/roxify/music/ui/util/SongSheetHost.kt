package com.roxify.music.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.LibraryIndex
import com.roxify.music.domain.model.Song
import com.roxify.music.ui.components.AddToPlaylistSheet
import com.roxify.music.ui.components.PlaylistNameDialog
import com.roxify.music.ui.components.SongActions
import com.roxify.music.ui.components.SongActionsSheet
import com.roxify.music.ui.navigation.Routes
import kotlinx.coroutines.launch

/**
 * Single place that renders the "track actions" flow (sheet → add-to-playlist → create
 * playlist). Every list screen reuses it, so behaviour stays consistent. Passing a null
 * [song] hides everything.
 */
@Composable
fun SongSheetHost(
    container: AppContainer,
    song: Song?,
    nav: NavController,
    onDismiss: () -> Unit,
    removeLabel: String = "Remove from list",
    onRemove: ((Song) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repository = container.musicRepository
    val playback = container.playbackManager
    val playlists by container.playlistRepository.playlists
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddToPlaylist by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val songId = song?.id ?: ""
    val isFavorite by remember(songId) { repository.isFavorite(songId) }
        .collectAsStateWithLifecycle(initialValue = false)

    if (song != null) {
        if (showAddToPlaylist) {
            AddToPlaylistSheet(
                song = song,
                playlists = playlists,
                onDismiss = {
                    showAddToPlaylist = false
                    onDismiss()
                },
                onCreateNew = { showCreateDialog = true },
                onPick = { playlist ->
                    scope.launch { container.playlistRepository.addSong(playlist.id, song) }
                    showAddToPlaylist = false
                    onDismiss()
                }
            )
        } else {
            SongActionsSheet(
                song = song,
                actions = SongActions(
                    isFavorite = isFavorite,
                    onPlayNext = { playback.playNext(song) },
                    onAddToQueue = { playback.appendToQueue(listOf(song)) },
                    onToggleFavorite = { scope.launch { repository.toggleFavorite(song) } },
                    onAddToPlaylist = { showAddToPlaylist = true },
                    onGoToAlbum = {
                        if (song.album.isNotBlank()) {
                            nav.navigate(Routes.album(LibraryIndex.albumKey(song.album, song.artist)))
                        }
                    },
                    onGoToArtist = {
                        if (song.artist.isNotBlank()) nav.navigate(Routes.artist(song.artist))
                    },
                    onRemove = onRemove?.let { callback -> { callback(song) } },
                    removeLabel = removeLabel
                ),
                onDismiss = onDismiss
            )
        }
    }

    if (showCreateDialog && song != null) {
        PlaylistNameDialog(
            title = "New playlist",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                scope.launch {
                    val id = container.playlistRepository.createPlaylist(name)
                    container.playlistRepository.addSong(id, song)
                }
                showCreateDialog = false
                showAddToPlaylist = false
                onDismiss()
            }
        )
    }
}
