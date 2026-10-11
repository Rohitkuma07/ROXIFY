package com.roxify.music.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.roxify.music.domain.model.RepeatMode
import com.roxify.music.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Client side wrapper around the Media3 [MediaController]. Owns all interaction with the
 * [PlaybackService]: queue management, transport controls, shuffle/repeat, the sleep
 * timer and a position ticker feeding the UI.
 */
class PlaybackManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controller: MediaController? = null
    private var connecting = false

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private var pendingPlay: Pair<List<Song>, Int>? = null
    private var positionJob: Job? = null
    private var sleepJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState()
            updateQueue()
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update { it.copy(errorMessage = error.message ?: "Playback error") }
        }
    }

    // ------------------------------------------------------------------ lifecycle

    fun connect() {
        if (controller != null || connecting) return
        connecting = true
        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                connecting = false
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        mediaController.addListener(listener)
                        _uiState.update { it.copy(isConnected = true) }
                        startTicker()
                        pendingPlay?.let { (songs, index) ->
                            pendingPlay = null
                            playSongs(songs, index)
                        }
                        syncState()
                        updateQueue()
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(errorMessage = error.message ?: "Could not start playback service")
                        }
                    }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun release() {
        positionJob?.cancel()
        sleepJob?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        _uiState.value = PlayerUiState()
    }

    // ------------------------------------------------------------------ transport

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val mediaController = controller
        if (mediaController == null) {
            pendingPlay = songs to startIndex
            connect()
            return
        }
        val items = MediaItemMapper.toMediaItems(songs)
        val safeIndex = startIndex.coerceIn(0, items.lastIndex)
        mediaController.setMediaItems(items, safeIndex, 0L)
        mediaController.prepare()
        mediaController.play()
        updateQueue()
        syncState()
    }

    fun playSong(song: Song) = playSongs(listOf(song), 0)

    fun appendToQueue(songs: List<Song>) {
        val mediaController = controller ?: return
        if (mediaController.mediaItemCount == 0) {
            playSongs(songs, 0)
            return
        }
        mediaController.addMediaItems(MediaItemMapper.toMediaItems(songs))
        updateQueue()
        syncState()
    }

    fun playNext(song: Song) {
        val mediaController = controller ?: return
        val insertIndex = (mediaController.currentMediaItemIndex + 1)
            .coerceIn(0, mediaController.mediaItemCount)
        mediaController.addMediaItem(insertIndex, MediaItemMapper.toMediaItem(song))
        updateQueue()
        syncState()
    }

    fun togglePlayPause() {
        val mediaController = controller ?: run { connect(); return }
        if (mediaController.isPlaying) mediaController.pause() else {
            if (mediaController.playbackState == Player.STATE_IDLE) mediaController.prepare()
            mediaController.play()
        }
        syncState()
    }

    fun play() {
        controller?.play()
        syncState()
    }

    fun pause() {
        controller?.pause()
        syncState()
    }

    fun next() {
        controller?.let {
            if (it.hasNextMediaItem()) it.seekToNextMediaItem()
        }
        syncState()
    }

    fun previous() {
        val mediaController = controller ?: return
        if (mediaController.currentPosition > 3_000L) {
            mediaController.seekTo(0L)
        } else if (mediaController.hasPreviousMediaItem()) {
            mediaController.seekToPreviousMediaItem()
        } else {
            mediaController.seekTo(0L)
        }
        syncState()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        syncState()
    }

    fun seekToIndex(index: Int) {
        val mediaController = controller ?: return
        mediaController.seekTo(index.coerceIn(0, (mediaController.mediaItemCount - 1).coerceAtLeast(0)), 0L)
        mediaController.play()
        syncState()
    }

    // ------------------------------------------------------------------ modes

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
        syncState()
    }

    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (RepeatMode.fromInt(it.repeatMode).next()) {
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            }
        }
        syncState()
    }

    fun applyDefaults(shuffle: Boolean, repeat: RepeatMode) {
        controller?.let {
            it.shuffleModeEnabled = shuffle
            it.repeatMode = when (repeat) {
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            }
        }
        syncState()
    }

    // ------------------------------------------------------------------ queue edits

    fun removeFromQueue(index: Int) {
        val mediaController = controller ?: return
        if (index in 0 until mediaController.mediaItemCount) {
            mediaController.removeMediaItem(index)
            updateQueue()
            syncState()
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val mediaController = controller ?: return
        if (fromIndex in 0 until mediaController.mediaItemCount &&
            toIndex in 0 until mediaController.mediaItemCount
        ) {
            mediaController.moveMediaItem(fromIndex, toIndex)
            updateQueue()
            syncState()
        }
    }

    fun clearQueue() {
        controller?.clearMediaItems()
        updateQueue()
        syncState()
    }

    fun removeQueueItemById(songId: String) {
        val index = _queue.value.indexOfFirst { it.id == songId }
        if (index >= 0) removeFromQueue(index)
    }

    // ------------------------------------------------------------------ sleep timer

    fun startSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        if (minutes <= 0) {
            _uiState.update { it.copy(sleepTimerRemainingMs = 0L) }
            return
        }
        val endAt = System.currentTimeMillis() + minutes * 60_000L
        sleepJob = scope.launch {
            while (isActive) {
                val remaining = endAt - System.currentTimeMillis()
                _uiState.update { it.copy(sleepTimerRemainingMs = remaining.coerceAtLeast(0L)) }
                if (remaining <= 0L) {
                    controller?.pause()
                    break
                }
                delay(1_000L)
            }
            _uiState.update { it.copy(sleepTimerRemainingMs = 0L) }
        }
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _uiState.update { it.copy(sleepTimerRemainingMs = 0L) }
    }

    // ------------------------------------------------------------------ internals

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun startTicker() {
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (isActive) {
                val mediaController = controller
                if (mediaController != null && mediaController.isPlaying) {
                    _uiState.update {
                        it.copy(
                            positionMs = mediaController.currentPosition.coerceAtLeast(0L),
                            bufferedMs = mediaController.bufferedPosition.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    private fun syncState() {
        val mediaController = controller ?: return
        val song = MediaItemMapper.fromMediaItem(mediaController.currentMediaItem)
        val duration = mediaController.duration.takeIf { it > 0L } ?: (song?.durationMs ?: 0L)
        _uiState.update { prev ->
            prev.copy(
                isConnected = true,
                isPlaying = mediaController.isPlaying,
                isBuffering = mediaController.playbackState == Player.STATE_BUFFERING,
                currentSong = song ?: prev.currentSong,
                currentIndex = mediaController.currentMediaItemIndex,
                positionMs = mediaController.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                bufferedMs = mediaController.bufferedPosition.coerceAtLeast(0L),
                shuffle = mediaController.shuffleModeEnabled,
                repeatMode = RepeatMode.fromInt(mediaController.repeatMode),
                hasNext = mediaController.hasNextMediaItem(),
                hasPrevious = mediaController.hasPreviousMediaItem(),
                queueSize = mediaController.mediaItemCount
            )
        }
    }

    private fun updateQueue() {
        val mediaController = controller ?: return
        val items = (0 until mediaController.mediaItemCount).mapNotNull { index ->
            MediaItemMapper.fromMediaItem(mediaController.getMediaItemAt(index))
        }
        _queue.value = items
    }

    /** Test/preview seam: allows injecting a fake queue without a live controller. */
    internal fun replaceQueueForPreview(items: List<MediaItem>) {
        _queue.value = MediaItemMapper.fromMediaItems(items)
    }
}
