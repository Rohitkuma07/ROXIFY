package com.roxify.music.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.SongSort
import com.roxify.music.ui.components.AlbumCard
import com.roxify.music.ui.components.ArtistCard
import com.roxify.music.ui.components.PlaylistNameDialog
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.SectionHeader
import com.roxify.music.ui.components.SongTile
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.ConnectedTrackList
import com.roxify.music.ui.util.rememberRoxifyViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val container: AppContainer) : ViewModel() {

    private val music = container.musicRepository
    private val playlists = container.playlistRepository

    val sort = mutableStateOf(SongSort.TITLE)

    val songs = music.sortedSongs(SongSort.TITLE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums = music.albums.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val artists = music.artists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val folders = music.folders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allPlaylists = playlists.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteCount = music.favoriteCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun createPlaylist(name: String) = viewModelScope.launch {
        playlists.createPlaylist(name)
    }

    fun deletePlaylist(id: Long) = viewModelScope.launch {
        playlists.deletePlaylist(id)
    }
}

private val tabs = listOf("Songs", "Albums", "Artists", "Folders", "Playlists")

@Composable
fun LibraryScreen(nav: NavController, container: AppContainer, settings: AppSettings) {
    val viewModel = rememberRoxifyViewModel { LibraryViewModel(it) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreate by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SongSort.TITLE) }
    val scope = rememberCoroutineScope()

    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Your Library",
            actions = {
                Box {
                    IconButton(onClick = { sortMenu = true }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort songs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        SongSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sortOption = option
                                    sortMenu = false
                                    viewModel.sort.value = option
                                }
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickAccessCard("Favourites", Icons.Rounded.FavoriteBorder, Modifier.weight(1f)) {
                nav.navigate(Routes.FAVORITES)
            }
            QuickAccessCard("Recent", Icons.Rounded.History, Modifier.weight(1f)) {
                nav.navigate(Routes.RECENT)
            }
            QuickAccessCard("On device", Icons.Rounded.MusicNote, Modifier.weight(1f)) {
                nav.navigate(Routes.LOCAL_MUSIC)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (selectedTab) {
            0 -> if (songs.isEmpty()) {
                TrackListEmpty(
                    title = "Your library is empty",
                    message = "Grant audio access and refresh, or enjoy the DEMO tracks.",
                    modifier = Modifier.height(300.dp)
                )
            } else {
                ConnectedTrackList(
                    container = container,
                    nav = nav,
                    songs = if (sortOption == SongSort.TITLE) songs
                    else com.roxify.music.domain.LibraryIndex.sort(songs, sortOption),
                    favoriteIds = favoriteIds
                )
            }

            1 -> if (albums.isEmpty()) {
                TrackListEmpty(
                    title = "No albums",
                    message = "Albums from your device will show up here.",
                    modifier = Modifier.height(300.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { nav.navigate(Routes.album(album.id)) }
                        )
                    }
                }
            }

            2 -> if (artists.isEmpty()) {
                TrackListEmpty(
                    title = "No artists",
                    message = "Artists from your device will show up here.",
                    modifier = Modifier.height(300.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(artists, key = { it.id }) { artist ->
                        ArtistCard(
                            artist = artist,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { nav.navigate(Routes.artist(artist.id)) }
                        )
                    }
                }
            }

            3 -> if (folders.isEmpty()) {
                TrackListEmpty(
                    title = "No folders",
                    message = "Folder information is only available for device audio.",
                    modifier = Modifier.height(300.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(folders, key = { it.id }) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    folder.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${folder.songCount} songs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Playlists",
                        actionLabel = "Create",
                        onAction = { showCreate = true }
                    )
                }
                if (playlists.isEmpty()) {
                    item {
                        TrackListEmpty(
                            title = "No playlists yet",
                            message = "Create your first playlist to organise your music.",
                            modifier = Modifier.height(260.dp)
                        )
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        SongTile(
                            title = playlist.name,
                            subtitle = "${playlist.songCount} songs",
                            onClick = { nav.navigate(Routes.playlist(playlist.id)) }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Create playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        PlaylistNameDialog(
            title = "New playlist",
            confirmLabel = "Create",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                scope.launch { container.playlistRepository.createPlaylist(name) }
                showCreate = false
            }
        )
    }
}

@Composable
private fun QuickAccessCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
