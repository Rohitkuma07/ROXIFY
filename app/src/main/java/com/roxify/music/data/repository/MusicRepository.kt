package com.roxify.music.data.repository

import android.content.Context
import com.roxify.music.data.local.DemoCatalog
import com.roxify.music.data.local.FavoriteDao
import com.roxify.music.data.local.FavoriteEntity
import com.roxify.music.data.local.LocalMusicDataSource
import com.roxify.music.data.local.RecentDao
import com.roxify.music.data.local.RecentEntity
import com.roxify.music.data.mapper.SongJson
import com.roxify.music.data.prefs.SettingsRepository
import com.roxify.music.domain.LibraryIndex
import com.roxify.music.domain.model.Album
import com.roxify.music.domain.model.Artist
import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.MusicFolder
import com.roxify.music.domain.model.SearchResults
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSort
import com.roxify.music.domain.model.SongSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single source of truth for the music library: device audio (MediaStore), the bundled
 * demo catalogue, favourites and recently played history.
 */
class MusicRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao,
    private val settingsRepository: SettingsRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val localSource = LocalMusicDataSource(context)

    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    private val _lastScanError = MutableStateFlow<String?>(null)
    private val _hasScanned = MutableStateFlow(false)

    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    val lastScanError: StateFlow<String?> = _lastScanError.asStateFlow()
    val hasScanned: StateFlow<Boolean> = _hasScanned.asStateFlow()

    private val demoEnabled: StateFlow<Boolean> = settingsRepository.settings
        .map { it.showDemoContent }
        .stateIn(scope, SharingStarted.Eagerly, true)

    /** Everything playable: device audio first, then (labelled) demo tracks. */
    val allSongs: StateFlow<List<Song>> =
        combine(_deviceSongs, demoEnabled) { device, showDemo ->
            if (showDemo) device + DemoCatalog.songs else device
        }.stateIn(scope, SharingStarted.Eagerly, DemoCatalog.songs)

    val deviceSongs: StateFlow<List<Song>> = _deviceSongs.asStateFlow()

    val albums: Flow<List<Album>> = allSongs.map { LibraryIndex.albums(it) }
    val artists: Flow<List<Artist>> = allSongs.map { LibraryIndex.artists(it) }
    val folders: Flow<List<MusicFolder>> = allSongs.map { LibraryIndex.folders(it) }

    val favorites: Flow<List<Song>> = favoriteDao.observeAll()
        .map { list -> list.mapNotNull { SongJson.decode(it.songJson) } }

    val favoriteIds: Flow<Set<String>> = favoriteDao.observeAll()
        .map { list -> list.map { it.trackId }.toSet() }

    val favoriteCount: Flow<Int> = favoriteDao.observeCount()

    val recentlyPlayed: Flow<List<Song>> = recentDao.observeAll()
        .map { list -> list.mapNotNull { SongJson.decode(it.songJson) } }

    val recentlyPlayedLimited: Flow<List<Song>> = recentDao.observeRecent(20)
        .map { list -> list.mapNotNull { SongJson.decode(it.songJson) } }

    fun isFavorite(trackId: String): Flow<Boolean> = favoriteDao.observeIsFavorite(trackId)

    // ---------------------------------------------------------------- scanning

    /** Rescans the device for audio. Safe to call repeatedly. */
    fun refreshLibrary() {
        scope.launch {
            _isScanning.value = true
            _lastScanError.value = null
            runCatching { localSource.loadSongs() }
                .onSuccess {
                    _deviceSongs.value = it
                    _hasScanned.value = true
                }
                .onFailure { _lastScanError.value = it.message ?: "Could not read device music" }
            _isScanning.value = false
        }
    }

    // ---------------------------------------------------------------- library views

    suspend fun songsForMood(mood: Mood): List<Song> = allSongs.value.filter { song ->
        val genre = song.genre?.lowercase()
        genre == mood.label.lowercase() ||
            (song.source == SongSource.DEMO && song.genre.equals(mood.label, true))
    }

    fun sortedSongs(sort: SongSort): Flow<List<Song>> = allSongs.map { LibraryIndex.sort(it, sort) }

    /** Deterministic "trending" list: newest device tracks first, then demo tracks. */
    fun trending(limit: Int = 12): Flow<List<Song>> = allSongs.map { songs ->
        songs.sortedWith(
            compareByDescending<Song> { it.dateAdded }
                .thenBy { it.title.lowercase() }
        ).take(limit)
    }

    /** Simple offline recommendation: a stable, mood-diverse mix. */
    fun recommended(limit: Int = 12): Flow<List<Song>> = allSongs.map { songs ->
        songs.sortedBy { it.id.hashCode() }.take(limit)
    }

    suspend fun songById(id: String): Song? = allSongs.value.firstOrNull { it.id == id }

    suspend fun songsForAlbum(albumId: String): List<Song> =
        allSongs.value.filter { LibraryIndex.albumKey(it.album, it.artist) == albumId }
            .sortedBy { it.trackNumber }

    suspend fun songsForArtist(artistId: String): List<Song> =
        allSongs.value.filter { it.artist == artistId }.sortedBy { it.album }

    // ---------------------------------------------------------------- search

    suspend fun search(query: String): SearchResults = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext SearchResults(query)
        val songs = allSongs.value
        SearchResults(
            query = query,
            songs = songs.filter { LibraryIndex.songMatches(it, query) }.take(50),
            albums = LibraryIndex.albums(songs).filter {
                it.title.contains(query, true) || it.artist.contains(query, true)
            }.take(20),
            artists = LibraryIndex.artists(songs).filter {
                it.name.contains(query, true)
            }.take(20),
            playlists = emptyList()
        )
    }

    // ---------------------------------------------------------------- favourites

    suspend fun toggleFavorite(song: Song) {
        if (favoriteDao.isFavorite(song.id)) {
            favoriteDao.delete(song.id)
        } else {
            favoriteDao.insert(
                FavoriteEntity(song.id, SongJson.encode(song), System.currentTimeMillis())
            )
        }
    }

    suspend fun addFavorite(song: Song) {
        favoriteDao.insert(
            FavoriteEntity(song.id, SongJson.encode(song), System.currentTimeMillis())
        )
    }

    suspend fun removeFavorite(trackId: String) = favoriteDao.delete(trackId)

    // ---------------------------------------------------------------- history

    suspend fun markPlayed(song: Song) {
        recentDao.insert(
            RecentEntity(song.id, SongJson.encode(song), System.currentTimeMillis())
        )
    }

    suspend fun clearRecentlyPlayed() = recentDao.clear()
}
