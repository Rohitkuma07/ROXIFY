package com.roxify.music.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.data.prefs.AudioQuality
import com.roxify.music.data.prefs.ThemeMode
import com.roxify.music.di.AppContainer
import com.roxify.music.domain.model.RepeatMode
import com.roxify.music.ui.components.GlassCard
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.theme.NeonPurple
import kotlinx.coroutines.launch

/** All persisted preferences: theme, playback defaults, data and shortcuts. */
@Composable
fun SettingsScreen(nav: NavController, container: AppContainer, settings: AppSettings) {
    val repository = container.settingsRepository
    val scope = rememberCoroutineScope()
    var qualityMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        RoxifyTopBar(title = "Settings", onBack = { nav.popBackStack() })

        SettingsSection("Appearance") {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { scope.launch { repository.setThemeMode(mode) } },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SettingSwitchRow(
                title = "Dynamic colour",
                subtitle = "Tint accents with your wallpaper (ROXIFY keeps its brand purple)",
                checked = settings.useDynamicColor,
                onCheckedChange = { scope.launch { repository.setDynamicColor(it) } }
            )
        }

        SettingsSection("Playback") {
            SettingSwitchRow(
                title = "Gapless playback",
                subtitle = "Play tracks back-to-back without silence",
                checked = settings.gaplessPlayback,
                onCheckedChange = { scope.launch { repository.setGapless(it) } }
            )
            SettingSwitchRow(
                title = "Shuffle by default",
                subtitle = "Start every queue shuffled",
                checked = settings.defaultShuffle,
                onCheckedChange = { scope.launch { repository.setDefaultShuffle(it) } }
            )
            Text("Repeat by default", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.defaultRepeat == mode,
                        onClick = { scope.launch { repository.setDefaultRepeat(mode) } },
                        label = {
                            Text(
                                when (mode) {
                                    RepeatMode.OFF -> "Off"
                                    RepeatMode.ALL -> "All"
                                    RepeatMode.ONE -> "One"
                                }
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Crossfade: ${settings.crossfadeSeconds}s",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = settings.crossfadeSeconds.toFloat(),
                onValueChange = { scope.launch { repository.setCrossfade(it.toInt()) } },
                valueRange = 0f..12f,
                steps = 11
            )
        }

        SettingsSection("Audio quality") {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { qualityMenu = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Streaming quality", style = MaterialTheme.typography.titleMedium)
                        Text(
                            settings.audioQuality.label,
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
                DropdownMenu(expanded = qualityMenu, onDismissRequest = { qualityMenu = false }) {
                    AudioQuality.entries.forEach { quality ->
                        DropdownMenuItem(
                            text = { Text(quality.label) },
                            onClick = {
                                scope.launch { repository.setAudioQuality(quality) }
                                qualityMenu = false
                            }
                        )
                    }
                }
            }
        }

        SettingsSection("Content & data") {
            SettingSwitchRow(
                title = "Show demo library",
                subtitle = "Include the bundled, clearly-labelled DEMO tracks",
                checked = settings.showDemoContent,
                onCheckedChange = { scope.launch { repository.setShowDemoContent(it) } }
            )
            ShortcutRow(Icons.Rounded.Equalizer, "Equalizer", "Open the equalizer") {
                nav.navigate(Routes.EQUALIZER)
            }
            ShortcutRow(Icons.Rounded.Security, "Permissions", "Audio access and privacy") {
                nav.navigate(Routes.PERMISSIONS)
            }
        }

        SettingsSection("Database") {
            Text(
                "Reset preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Restores every setting to its default. Your playlists and favourites are kept.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.clickable { showResetDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Reset now", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(120.dp))
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset preferences?") },
            text = { Text("This restores the default theme, playback and content settings.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        scope.launch { repository.resetPreferences() }
                        showResetDialog = false
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NeonPurple,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonPurple)
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
