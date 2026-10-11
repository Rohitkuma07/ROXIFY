package com.roxify.music.ui.screens.queue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.SongRow
import com.roxify.music.ui.components.TrackListEmpty

/** The current play queue with reorder-free management (play, remove, clear, shuffle). */
@Composable
fun QueueScreen(nav: NavController, container: AppContainer) {
    val playback = container.playbackManager
    val state by playback.uiState.collectAsStateWithLifecycle()
    val queue by playback.queue.collectAsStateWithLifecycle()

    val currentIndex = state.currentIndex.coerceAtLeast(0)

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Queue",
            subtitle = "${queue.size} tracks${if (state.shuffle) " • shuffled" else ""}",
            onBack = { nav.popBackStack() },
            actions = {
                IconButton(onClick = { playback.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffle) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { playback.clearQueue() }, enabled = queue.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.ClearAll,
                        contentDescription = "Clear queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        if (queue.isEmpty()) {
            TrackListEmpty(
                title = "Nothing in the queue",
                message = "Start playing a song and it will show up here.",
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                modifier = Modifier.height(320.dp)
            )
            return@Column
        }

        Text(
            text = "Now playing${if (currentIndex in queue.indices) " • ${currentIndex + 1} of ${queue.size}" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                val isCurrent = index == currentIndex
                SongRow(
                    song = song,
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && state.isPlaying,
                    onClick = { playback.seekToIndex(index) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    trailing = {
                        IconButton(
                            onClick = { playback.removeFromQueue(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Remove from queue",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}
