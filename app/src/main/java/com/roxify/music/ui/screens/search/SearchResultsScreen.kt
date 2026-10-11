package com.roxify.music.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.SearchResults
import com.roxify.music.ui.components.AlbumCard
import com.roxify.music.ui.components.LoadingState
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.SectionHeader
import com.roxify.music.ui.components.SongRow
import com.roxify.music.ui.components.SongTile
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.rememberRoxifyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchResultsViewModel(
    private val container: AppContainer,
    private val query: String
) : ViewModel() {

    private val _results = MutableStateFlow<SearchResults?>(null)
    val results = _results.asStateFlow()

    init {
        viewModelScope.launch {
            val base = container.musicRepository.search(query)
            val playlists = container.playlistRepository.playlists.first()
                .filter { it.name.contains(query, ignoreCase = true) }
            _results.value = base.copy(playlists = playlists)
        }
    }
}

@Composable
fun SearchResultsScreen(nav: NavController, container: AppContainer, query: String) {
    val viewModel = rememberRoxifyViewModel(key = "search-$query") {
        SearchResultsViewModel(it, query)
    }
    val results by viewModel.results.collectAsStateWithLifecycle()
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val playerState by container.playbackManager.uiState.collectAsStateWithLifecycle()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Results",
            subtitle = "“$query”",
            onBack = { nav.popBackStack() }
        )

        val data = results
        if (data == null) {
            LoadingState(message = "Searching…", modifier = Modifier.height(280.dp))
            return@Column
        }

        if (data.isEmpty) {
            TrackListEmpty(
                title = "No matches",
                message = "Nothing in your library matches “$query”.",
                icon = Icons.Rounded.SearchOff,
                modifier = Modifier.height(320.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(title = "Songs (${data.songs.size})")
            }
            itemsIndexed(data.songs, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isCurrent = playerState.currentSong?.id == song.id,
                    isPlaying = playerState.currentSong?.id == song.id && playerState.isPlaying,
                    isFavorite = song.id in favoriteIds,
                    onClick = {
                        container.playbackManager.playSongs(data.songs, index)
                        nav.navigate(Routes.PLAYER)
                    },
                    onToggleFavorite = {
                        scope.launch { container.musicRepository.toggleFavorite(song) }
                    }
                )
            }

            if (data.artists.isNotEmpty()) {
                item { SectionHeader(title = "Artists (${data.artists.size})") }
                items(data.artists, key = { it.id }) { artist ->
                    SongTile(
                        title = artist.name,
                        subtitle = "${artist.songCount} songs • ${artist.albumCount} albums",
                        onClick = { nav.navigate(Routes.artist(artist.id)) }
                    )
                }
            }

            if (data.albums.isNotEmpty()) {
                item { SectionHeader(title = "Albums (${data.albums.size})") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(data.albums, key = { it.id }) { album ->
                            AlbumCard(album = album, onClick = { nav.navigate(Routes.album(album.id)) })
                        }
                    }
                }
            }

            if (data.playlists.isNotEmpty()) {
                item { SectionHeader(title = "Playlists (${data.playlists.size})") }
                items(data.playlists, key = { it.id }) { playlist ->
                    SongTile(
                        title = playlist.name,
                        subtitle = "${playlist.songCount} songs",
                        onClick = { nav.navigate(Routes.playlist(playlist.id)) }
                    )
                }
            }

            item { Box(Modifier.height(24.dp)) }
        }
    }
}
