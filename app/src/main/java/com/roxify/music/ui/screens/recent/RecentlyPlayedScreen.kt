package com.roxify.music.ui.screens.recent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.util.ConnectedTrackList
import kotlinx.coroutines.launch

@Composable
fun RecentlyPlayedScreen(nav: NavController, container: AppContainer) {
    val recents by container.musicRepository.recentlyPlayed
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Recently played",
            subtitle = "${recents.size} songs",
            onBack = { nav.popBackStack() },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { container.musicRepository.clearRecentlyPlayed() } },
                        enabled = recents.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
        if (recents.isEmpty()) {
            TrackListEmpty(
                title = "No listening history",
                message = "Songs you play will be listed here.",
                icon = Icons.Rounded.History,
                modifier = Modifier.height(320.dp)
            )
        } else {
            ConnectedTrackList(
                container = container,
                nav = nav,
                songs = recents,
                favoriteIds = favoriteIds
            )
        }
    }
}
