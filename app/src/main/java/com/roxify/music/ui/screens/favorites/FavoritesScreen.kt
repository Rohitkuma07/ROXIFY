package com.roxify.music.ui.screens.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
fun FavoritesScreen(nav: NavController, container: AppContainer) {
    val favorites by container.musicRepository.favorites
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Favourites",
            subtitle = "${favorites.size} songs",
            onBack = { nav.popBackStack() }
        )
        if (favorites.isEmpty()) {
            TrackListEmpty(
                title = "No favourites yet",
                message = "Tap the heart on any track to keep it here.",
                icon = Icons.Rounded.FavoriteBorder,
                modifier = Modifier.height(320.dp)
            )
        } else {
            ConnectedTrackList(
                container = container,
                nav = nav,
                songs = favorites,
                favoriteIds = favoriteIds,
                removeLabel = "Remove from favourites",
                onRemoveSong = { song ->
                    scope.launch { container.musicRepository.removeFavorite(song.id) }
                }
            )
        }
    }
}
