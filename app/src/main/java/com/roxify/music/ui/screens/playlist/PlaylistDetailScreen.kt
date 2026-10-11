package com.roxify.music.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.Song
import com.roxify.music.ui.components.ArtworkBox
import com.roxify.music.ui.components.GradientButton
import com.roxify.music.ui.components.InfoBanner
import com.roxify.music.ui.components.PlaylistNameDialog
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.ConnectedTrackList
import com.roxify.music.ui.util.rememberRoxifyViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(container: AppContainer, private val playlistId: Long) : ViewModel() {

    private val repository = container.playlistRepository

    val playlist = repository.observePlaylist(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val songs = repository.observeSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rename(name: String) = viewModelScope.launch { repository.renamePlaylist(playlistId, name) }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        repository.deletePlaylist(playlistId)
        onDone()
    }

    fun removeSong(song: Song) = viewModelScope.launch {
        repository.removeSong(playlistId, song.id)
    }
}

@Composable
fun PlaylistDetailScreen(nav: NavController, container: AppContainer, playlistId: Long) {
    val viewModel = rememberRoxifyViewModel(key = "playlist-$playlistId") {
        PlaylistDetailViewModel(it, playlistId)
    }
    val playlist by viewModel.playlist.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())

    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = playlist?.name ?: "Playlist",
            subtitle = "${songs.size} songs",
            onBack = { nav.popBackStack() },
            actions = {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Playlist options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename playlist") },
                            onClick = {
                                menuOpen = false
                                showRename = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete playlist") },
                            onClick = {
                                menuOpen = false
                                viewModel.delete { nav.popBackStack() }
                            }
                        )
                    }
                }
            }
        )

        if (songs.isEmpty()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ArtworkBox(
                    artworkUri = null,
                    seed = "playlist-$playlistId",
                    modifier = Modifier.size(150.dp),
                    cornerRadius = 22.dp
                )
                Spacer(Modifier.height(16.dp))
                InfoBanner(
                    title = "This playlist is empty",
                    message = "Use the “…” menu on any song and choose “Add to playlist”."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ArtworkBox(
                    artworkUri = songs.firstOrNull()?.artworkUri,
                    seed = "playlist-$playlistId",
                    modifier = Modifier.size(170.dp),
                    cornerRadius = 22.dp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = playlist?.name ?: "Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientButton(
                        text = "Play",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = {
                            container.playbackManager.playSongs(songs, 0)
                            nav.navigate(Routes.PLAYER)
                        }
                    )
                    GradientButton(
                        text = "Shuffle",
                        icon = Icons.Rounded.Shuffle,
                        onClick = {
                            container.playbackManager.playSongs(songs.shuffled(), 0)
                            nav.navigate(Routes.PLAYER)
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        Box(Modifier.weight(1f)) {
            ConnectedTrackList(
                container = container,
                nav = nav,
                songs = songs,
                favoriteIds = favoriteIds,
                removeLabel = "Remove from playlist",
                onRemoveSong = { viewModel.removeSong(it) }
            )
        }
    }

    if (showRename) {
        PlaylistNameDialog(
            title = "Rename playlist",
            confirmLabel = "Save",
            initialName = playlist?.name.orEmpty(),
            onDismiss = { showRename = false },
            onConfirm = { name ->
                viewModel.rename(name)
                showRename = false
            }
        )
    }
}
