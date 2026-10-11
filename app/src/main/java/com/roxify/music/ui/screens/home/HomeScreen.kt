package com.roxify.music.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.Song
import com.roxify.music.ui.components.AlbumCard
import com.roxify.music.ui.components.ArtistCard
import com.roxify.music.ui.components.ContinueCard
import com.roxify.music.ui.components.EmptyState
import com.roxify.music.ui.components.InfoBanner
import com.roxify.music.ui.components.LoadingState
import com.roxify.music.ui.components.MoodCard
import com.roxify.music.ui.components.SectionHeader
import com.roxify.music.ui.components.SongCard
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.util.SongSheetHost
import com.roxify.music.ui.util.rememberAudioPermissionRequest
import com.roxify.music.ui.util.rememberRoxifyViewModel
import com.roxify.music.util.Formatters
import com.roxify.music.util.MediaPermissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val music = container.musicRepository
    val playback = container.playbackManager

    val recentlyPlayed = music.recentlyPlayedLimited
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trending = music.trending(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recommended = music.recommended(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists = music.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums = music.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isScanning = music.isScanning
    val lastScanError = music.lastScanError

    fun refreshLibrary() = music.refreshLibrary()

    fun play(songs: List<Song>, index: Int) = playback.playSongs(songs, index)
}

@Composable
fun HomeScreen(
    nav: NavController,
    container: AppContainer,
    settings: AppSettings
) {
    val viewModel = rememberRoxifyViewModel { HomeViewModel(it) }
    val context = LocalContext.current

    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val trending by viewModel.trending.collectAsStateWithLifecycle()
    val recommended by viewModel.recommended.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanError by viewModel.lastScanError.collectAsStateWithLifecycle()
    val playerState by viewModel.playback.uiState.collectAsStateWithLifecycle()

    var actionSong by remember { mutableStateOf<Song?>(null) }
    var hasAudioPermission by remember { mutableStateOf(MediaPermissions.hasAudioPermission(context)) }

    val requestPermission = rememberAudioPermissionRequest { granted ->
        hasAudioPermission = granted
        if (granted) viewModel.refreshLibrary()
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!MediaPermissions.hasAudioPermission(context)) {
            requestPermission()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HomeHeader(settings, onSettings = { nav.navigate(Routes.SETTINGS) }) }

        if (isScanning) {
            item { LoadingState(message = "Scanning your device…", modifier = Modifier.height(220.dp)) }
        }
        if (scanError != null) {
            item {
                InfoBanner(
                    title = "Couldn't read device music",
                    message = scanError ?: ""
                )
            }
        }

        if (playerState.hasSong) {
            item {
                val song = playerState.currentSong!!
                ContinueCard(
                    song = song,
                    progress = playerState.progress,
                    onClick = { nav.navigate(Routes.PLAYER) }
                )
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(
                    title = "Recently played",
                    modifier = Modifier.weight(1f),
                    actionLabel = "See all",
                    onAction = { nav.navigate(Routes.RECENT) }
                )
                IconButton(onClick = { nav.navigate(Routes.RECENT) }) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            if (recentlyPlayed.isEmpty()) {
                InfoBanner(
                    title = "Nothing played yet",
                    message = "Start a track and it will show up here."
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentlyPlayed, key = { it.id }) { song ->
                        SongCard(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id && playerState.isPlaying,
                            onClick = {
                                viewModel.play(recentlyPlayed, recentlyPlayed.indexOf(song))
                                nav.navigate(Routes.PLAYER)
                            }
                        )
                    }
                }
            }
        }

        item { SectionHeader(title = "What's your mood?") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Mood.entries.toList(), key = { it.id }) { mood ->
                    MoodCard(mood = mood, onClick = { nav.navigate(Routes.searchResults(mood.label)) })
                }
            }
        }

        item {
            SectionHeader(
                title = "Trending now",
                actionLabel = "See all",
                onAction = { nav.navigate(Routes.searchResults("demo")) }
            )
        }
        item {
            SongCarousel(
                songs = trending,
                currentSongId = playerState.currentSong?.id,
                isPlaying = playerState.isPlaying,
                onPlay = { index ->
                    viewModel.play(trending, index)
                    nav.navigate(Routes.PLAYER)
                },
                onMore = { actionSong = it },
                onFavorite = { song -> scope.launch { container.musicRepository.toggleFavorite(song) } }
            )
        }

        item { SectionHeader(title = "Made for you") }
        item {
            SongCarousel(
                songs = recommended,
                currentSongId = playerState.currentSong?.id,
                isPlaying = playerState.isPlaying,
                onPlay = { index ->
                    viewModel.play(recommended, index)
                    nav.navigate(Routes.PLAYER)
                },
                onMore = { actionSong = it },
                onFavorite = { song -> scope.launch { container.musicRepository.toggleFavorite(song) } }
            )
        }

        item {
            SectionHeader(
                title = "Featured artists",
                actionLabel = "Library",
                onAction = { nav.navigate(Routes.LIBRARY) }
            )
        }
        item {
            if (artists.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.MusicOff,
                    title = "No artists yet",
                    message = "Grant audio access and refresh to see the artists on your device.",
                    modifier = Modifier.height(260.dp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(artists.take(12), key = { it.id }) { artist ->
                        ArtistCard(artist = artist, onClick = { nav.navigate(Routes.artist(artist.id)) })
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Featured albums",
                actionLabel = "Favourites",
                onAction = { nav.navigate(Routes.FAVORITES) }
            )
        }
        item {
            if (albums.isEmpty()) {
                InfoBanner(
                    title = "No albums yet",
                    message = "Albums from your device library will appear here."
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(albums.take(12), key = { it.id }) { album ->
                        AlbumCard(album = album, onClick = { nav.navigate(Routes.album(album.id)) })
                    }
                }
            }
        }

        item {
            InfoBanner(
                title = "About the demo tracks",
                message = "Tracks labelled DEMO are original royalty-free tones bundled with " +
                    "ROXIFY so the player works with no licence needed. No fake " +
                    "streaming results are ever shown."
            )
        }
    }

    SongSheetHost(
        container = container,
        song = actionSong,
        nav = nav,
        onDismiss = { actionSong = null }
    )
}

@Composable
private fun HomeHeader(settings: AppSettings, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = Formatters.greetingNow(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settings.userName.ifBlank { "Music Lover" },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = { }) {
            Icon(
                Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SongCarousel(
    songs: List<Song>,
    currentSongId: String?,
    isPlaying: Boolean,
    onPlay: (Int) -> Unit,
    onMore: (Song) -> Unit,
    onFavorite: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        InfoBanner(
            title = "Nothing here yet",
            message = "Refresh the library or add songs to your device."
        )
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            Column {
                SongCard(
                    song = song,
                    isPlaying = currentSongId == song.id && isPlaying,
                    onClick = { onPlay(songs.indexOf(song)) }
                )
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = { onFavorite(song) }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { onMore(song) }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(0.dp))
}
