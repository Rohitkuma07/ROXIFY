package com.roxify.music.ui.screens.local

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.SongSort
import com.roxify.music.ui.components.GradientButton
import com.roxify.music.ui.components.InfoBanner
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.TrackListEmpty
import com.roxify.music.ui.util.ConnectedTrackList
import com.roxify.music.ui.util.rememberAudioPermissionRequest
import com.roxify.music.util.MediaPermissions

@Composable
fun LocalMusicScreen(nav: NavController, container: AppContainer) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(MediaPermissions.hasAudioPermission(context)) }
    var sort by remember { mutableStateOf(SongSort.TITLE) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val deviceSongs by container.musicRepository.deviceSongs.collectAsStateWithLifecycle()
    val favoriteIds by container.musicRepository.favoriteIds
        .collectAsStateWithLifecycle(initialValue = emptySet())
    val isScanning by container.musicRepository.isScanning.collectAsStateWithLifecycle()

    val requestPermission = rememberAudioPermissionRequest { granted ->
        hasPermission = granted
        if (granted) container.musicRepository.refreshLibrary()
    }

    val sorted = remember(deviceSongs, sort) {
        com.roxify.music.domain.LibraryIndex.sort(deviceSongs, sort)
    }

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "On this device",
            subtitle = "${deviceSongs.size} songs found",
            onBack = { nav.popBackStack() },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(
                                Icons.Rounded.FolderOpen,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false }
                        ) {
                            SongSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sort = option
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { container.musicRepository.refreshLibrary() }) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sorted by ${sort.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isScanning) {
                Text(
                    "Scanning…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        when {
            !hasPermission -> {
                Column(Modifier.padding(16.dp)) {
                    InfoBanner(
                        title = "Audio access needed",
                        message = "ROXIFY needs permission to read the music stored on this " +
                            "device. Nothing is uploaded anywhere."
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    GradientButton(text = "Grant access", onClick = { requestPermission() })
                }
            }

            deviceSongs.isEmpty() && !isScanning -> {
                TrackListEmpty(
                    title = "No device music found",
                    message = "Add audio files to this device, then tap Refresh. You can still " +
                        "enjoy the bundled DEMO tracks from Home.",
                    modifier = Modifier.height(300.dp)
                )
            }

            else -> {
                ConnectedTrackList(
                    container = container,
                    nav = nav,
                    songs = sorted,
                    favoriteIds = favoriteIds
                )
            }
        }
    }
}
