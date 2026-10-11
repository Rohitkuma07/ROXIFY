package com.roxify.music.ui.screens.artist

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.LibraryIndex
import com.roxify.music.ui.components.AlbumCard
import com.roxify.music.ui.components.ArtworkBox
import com.roxify.music.ui.components.GradientButton
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.SectionHeader
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.ConnectedTrackList
import com.roxify.music.ui.util.rememberRoxifyViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ArtistDetailViewModel(container: AppContainer, private val artistId: String) : ViewModel() {
    private val artistSongs = container.musicRepository.allSongs
        .map { list -> list.filter { it.artist == artistId } }

    val songs = artistSongs
        .map { it.sortedBy { song -> song.album } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums = artistSongs
        .map { LibraryIndex.albums(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun ArtistDetailScreen(nav: NavController, container: AppContainer, artistId: String) {
    val viewModel = rememberRoxifyViewModel(key = "artist-$artistId") {
        ArtistDetailViewModel(it, artistId)
    }
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())

    val artwork = songs.firstOrNull { it.artworkUri != null }?.artworkUri

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(title = "Artist", onBack = { nav.popBackStack() })

        if (songs.isEmpty()) {
            TrackListEmpty(
                title = "Artist unavailable",
                message = "These tracks are no longer in your library.",
                modifier = Modifier.height(300.dp)
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArtworkBox(
                artworkUri = artwork,
                seed = artistId,
                modifier = Modifier.size(160.dp),
                cornerRadius = 100.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = artistId,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${songs.size} songs • ${albums.size} albums",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
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
            Spacer(Modifier.height(8.dp))
        }

        if (albums.isNotEmpty()) {
            SectionHeader(
                title = "Albums",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumCard(album = album, onClick = { nav.navigate(Routes.album(album.id)) })
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Box(Modifier.weight(1f)) {
            ConnectedTrackList(
                container = container,
                nav = nav,
                songs = songs,
                favoriteIds = favoriteIds
            )
        }
    }
}
