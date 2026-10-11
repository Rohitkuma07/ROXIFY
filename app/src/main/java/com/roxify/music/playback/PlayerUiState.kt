package com.roxify.music.playback

import com.roxify.music.domain.model.RepeatMode
import com.roxify.music.domain.model.Song

/** Immutable snapshot of everything the player UI needs to render. */
data class PlayerUiState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSong: Song? = null,
    val currentIndex: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val queueSize: Int = 0,
    val sleepTimerRemainingMs: Long = 0L,
    val errorMessage: String? = null
) {
    val hasSong: Boolean get() = currentSong != null

    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val isSleepTimerActive: Boolean get() = sleepTimerRemainingMs > 0L
}
