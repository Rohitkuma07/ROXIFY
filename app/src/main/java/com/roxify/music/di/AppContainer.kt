package com.roxify.music.di

import android.content.Context
import com.roxify.music.data.local.RoxifyDatabase
import com.roxify.music.data.prefs.SettingsRepository
import com.roxify.music.data.remote.OnlineMusicRepository
import com.roxify.music.data.repository.MusicRepository
import com.roxify.music.data.repository.PlaylistRepository
import com.roxify.music.playback.EqualizerController
import com.roxify.music.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Hand-rolled dependency container (no annotation processing required). Owns the
 * singletons for the whole app and wires the playback history recorder.
 */
class AppContainer(private val context: Context) {

    val database: RoxifyDatabase by lazy { RoxifyDatabase.get(context) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }

    val musicRepository: MusicRepository by lazy {
        MusicRepository(
            context = context,
            favoriteDao = database.favoriteDao(),
            recentDao = database.recentDao(),
            settingsRepository = settingsRepository
        )
    }

    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(database.playlistDao())
    }

    val onlineMusicRepository: OnlineMusicRepository by lazy { OnlineMusicRepository() }

    val playbackManager: PlaybackManager by lazy { PlaybackManager(context) }

    val equalizerController: EqualizerController by lazy { EqualizerController() }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    fun initialize() {
        if (started) return
        started = true

        playbackManager.connect()

        // Record "recently played" whenever the current track changes.
        scope.launch {
            playbackManager.uiState
                .map { it.currentSong?.id }
                .distinctUntilChanged()
                .collect { id ->
                    if (id != null) {
                        musicRepository.songById(id)?.let { musicRepository.markPlayed(it) }
                    }
                }
        }

        // Apply the user's default shuffle/repeat once the service is connected.
        scope.launch {
            val defaults = settingsRepository.settings.first()
            playbackManager.uiState.filter { it.isConnected }.first()
            playbackManager.applyDefaults(defaults.defaultShuffle, defaults.defaultRepeat)
        }
    }
}
