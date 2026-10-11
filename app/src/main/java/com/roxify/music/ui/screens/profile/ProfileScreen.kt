package com.roxify.music.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.ArtworkBox
import com.roxify.music.ui.components.GlassCard
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.components.StatTile
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.theme.NeonPurple
import kotlinx.coroutines.launch

/** Profile hub: identity, listening stats and quick links into the rest of the app. */
@Composable
fun ProfileScreen(nav: NavController, container: AppContainer, settings: AppSettings) {
    val music = container.musicRepository
    val scope = rememberCoroutineScope()

    val favorites by music.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val recents by music.recentlyPlayed.collectAsStateWithLifecycle(initialValue = emptyList())
    val deviceSongs by music.deviceSongs.collectAsStateWithLifecycle()
    val allSongs by music.allSongs.collectAsStateWithLifecycle()
    val playlists by container.playlistRepository.playlists
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var editingName by remember { mutableStateOf(false) }
    var nameDraft by remember(settings.userName) { mutableStateOf(settings.userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        RoxifyTopBar(title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArtworkBox(
                artworkUri = settings.userImageUri,
                seed = settings.userName,
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape),
                cornerRadius = 60.dp,
                iconFraction = 0.5f
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = settings.userName.ifBlank { "Music Lover" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { nameDraft = settings.userName; editingName = true }) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "Edit name",
                        tint = NeonPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = "Feel Every Beat",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                value = favorites.size.toString(),
                label = "Favourites",
                icon = Icons.Rounded.FavoriteBorder,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = playlists.size.toString(),
                label = "Playlists",
                icon = Icons.Rounded.LibraryMusic,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                value = allSongs.size.toString(),
                label = "Songs",
                icon = Icons.Rounded.MusicNote,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = recents.size.toString(),
                label = "Recently played",
                icon = Icons.Rounded.History,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileMenuRow(Icons.Rounded.FavoriteBorder, "Favourites", "${favorites.size} songs") {
                nav.navigate(Routes.FAVORITES)
            }
            ProfileMenuRow(Icons.Rounded.History, "Recently played", "${recents.size} songs") {
                nav.navigate(Routes.RECENT)
            }
            ProfileMenuRow(
                Icons.Rounded.MusicNote,
                "On this device",
                "${deviceSongs.size} songs"
            ) { nav.navigate(Routes.LOCAL_MUSIC) }
            ProfileMenuRow(Icons.Rounded.Equalizer, "Equalizer", "Bass, presets and bands") {
                nav.navigate(Routes.EQUALIZER)
            }
            ProfileMenuRow(Icons.Rounded.Security, "Permissions", "Audio access and privacy") {
                nav.navigate(Routes.PERMISSIONS)
            }
            ProfileMenuRow(Icons.Rounded.Settings, "Settings", "Theme, playback, data") {
                nav.navigate(Routes.SETTINGS)
            }
            ProfileMenuRow(Icons.Rounded.Info, "About ROXIFY", "Version and licences") {
                nav.navigate(Routes.ABOUT)
            }
        }

        Spacer(Modifier.height(120.dp))
    }

    if (editingName) {
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Your name") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    singleLine = true,
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { container.settingsRepository.setUserName(nameDraft.trim()) }
                        editingName = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingName = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 18,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.16f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
