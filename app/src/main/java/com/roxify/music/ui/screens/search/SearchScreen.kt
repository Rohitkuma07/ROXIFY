package com.roxify.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.SearchResults
import com.roxify.music.ui.components.AlbumCard
import com.roxify.music.ui.components.ArtistCard
import com.roxify.music.ui.components.MoodCard
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.SectionHeader
import com.roxify.music.ui.components.SongTile
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.rememberRoxifyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(private val container: AppContainer) : ViewModel() {

    private val music = container.musicRepository

    val recentSearches = container.settingsRepository.settings
        .map { it.recentSearches }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val featuredArtists = music.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _results = MutableStateFlow<SearchResults?>(null)
    val results = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _results.value = null
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val base = music.search(query)
            val playlists = container.playlistRepository.playlists.first()
                .filter { it.name.contains(query, ignoreCase = true) }
            _results.value = base.copy(playlists = playlists)
            _isSearching.value = false
        }
    }

    fun commitSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch { container.settingsRepository.addRecentSearch(query) }
        search(query)
    }

    fun clearRecentSearches() {
        viewModelScope.launch { container.settingsRepository.clearRecentSearches() }
    }
}

@Composable
fun SearchScreen(nav: NavController, container: AppContainer) {
    val viewModel = rememberRoxifyViewModel { SearchViewModel(it) }
    var query by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val results by viewModel.results.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val artists by viewModel.featuredArtists.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(title = "Search")

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                submitted = false
                viewModel.search(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Songs, artists, albums…") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        viewModel.search("")
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = {
                    submitted = true
                    viewModel.commitSearch(query)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors()
        )

        Spacer(Modifier.height(8.dp))

        when {
            query.isBlank() -> SearchLanding(
                nav = nav,
                recentSearches = recentSearches,
                artists = artists,
                onClearRecent = { viewModel.clearRecentSearches() },
                onSearchTerm = {
                    query = it
                    viewModel.commitSearch(it)
                }
            )

            results == null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(32.dp))
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Searching…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            results!!.isEmpty -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(32.dp))
                    Icon(
                        Icons.Rounded.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No results for “$query”",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> SearchResultsList(
                results = results!!,
                nav = nav,
                onSeeAll = {
                    submitted = true
                    viewModel.commitSearch(query)
                    nav.navigate(Routes.searchResults(query))
                },
                onPlay = { list, index ->
                    container.playbackManager.playSongs(list, index)
                    nav.navigate(Routes.PLAYER)
                }
            )
        }
    }
}

@Composable
private fun SearchLanding(
    nav: NavController,
    recentSearches: List<String>,
    artists: List<com.roxify.music.domain.model.Artist>,
    onClearRecent: () -> Unit,
    onSearchTerm: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent searches",
                    actionLabel = "Clear",
                    onAction = onClearRecent
                )
            }
            items(recentSearches) { term ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSearchTerm(term) }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(term, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item { SectionHeader(title = "Browse moods") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Mood.entries.toList(), key = { it.id }) { mood ->
                    MoodCard(mood = mood, onClick = { nav.navigate(Routes.searchResults(mood.label)) })
                }
            }
        }

        if (artists.isNotEmpty()) {
            item { SectionHeader(title = "Artists to explore") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(artists.take(12), key = { it.id }) { artist ->
                        ArtistCard(artist = artist, onClick = { nav.navigate(Routes.artist(artist.id)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: SearchResults,
    nav: NavController,
    onSeeAll: () -> Unit,
    onPlay: (List<com.roxify.music.domain.model.Song>, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (results.songs.isNotEmpty()) {
            item {
                SectionHeader(title = "Songs", actionLabel = "See all", onAction = onSeeAll)
            }
            items(results.songs.take(6), key = { it.id }) { song ->
                SongTile(
                    title = song.title,
                    subtitle = song.artist,
                    onClick = { onPlay(results.songs, results.songs.indexOf(song)) }
                )
            }
        }

        if (results.artists.isNotEmpty()) {
            item { SectionHeader(title = "Artists") }
            items(results.artists.take(6), key = { it.id }) { artist ->
                SongTile(
                    title = artist.name,
                    subtitle = "${artist.songCount} songs",
                    onClick = { nav.navigate(Routes.artist(artist.id)) }
                )
            }
        }

        if (results.albums.isNotEmpty()) {
            item { SectionHeader(title = "Albums") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results.albums, key = { it.id }) { album ->
                        AlbumCard(album = album, onClick = { nav.navigate(Routes.album(album.id)) })
                    }
                }
            }
        }

        if (results.playlists.isNotEmpty()) {
            item { SectionHeader(title = "Playlists") }
            items(results.playlists, key = { it.id }) { playlist ->
                SongTile(
                    title = playlist.name,
                    subtitle = "${playlist.songCount} songs",
                    onClick = { nav.navigate(Routes.playlist(playlist.id)) }
                )
            }
        }

        item { Box(Modifier.height(20.dp)) }
    }
}
