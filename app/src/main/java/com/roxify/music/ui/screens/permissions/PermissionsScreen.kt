package com.roxify.music.ui.screens.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.GlassCard
import com.roxify.music.ui.components.GradientButton
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.theme.NeonPurple
import com.roxify.music.ui.theme.RoxifyError
import com.roxify.music.ui.theme.RoxifySuccess
import com.roxify.music.ui.util.rememberAudioPermissionRequest
import com.roxify.music.util.MediaPermissions

/**
 * Transparent permissions hub. Explains exactly what ROXIFY needs and why, and never
 * blocks the bundled demo library when audio access is declined.
 */
@Composable
fun PermissionsScreen(nav: NavController, container: AppContainer) {
    val context = LocalContext.current
    var audioGranted by remember { mutableStateOf(MediaPermissions.hasAudioPermission(context)) }
    var notificationsGranted by remember {
        mutableStateOf(MediaPermissions.hasNotificationPermission(context))
    }

    val request = rememberAudioPermissionRequest { granted ->
        audioGranted = granted
        notificationsGranted = MediaPermissions.hasNotificationPermission(context)
        if (granted) container.musicRepository.refreshLibrary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        RoxifyTopBar(
            title = "Permissions",
            subtitle = "Your privacy, explained",
            onBack = { nav.popBackStack() }
        )

        PermissionRow(
            icon = Icons.Rounded.MusicNote,
            title = "Read your audio",
            permission = MediaPermissions.audioPermission.substringAfterLast('.'),
            granted = audioGranted,
            reason = "Required to list and play the music stored on this device. ROXIFY reads " +
                "the media library only — it never modifies your files."
        )

        Spacer(Modifier.height(12.dp))

        PermissionRow(
            icon = Icons.Rounded.Notifications,
            title = "Notifications",
            permission = "POST_NOTIFICATIONS",
            granted = notificationsGranted,
            reason = "Used for the playback notification and lock-screen controls. Optional — " +
                "the player still works without it."
        )

        Spacer(Modifier.height(20.dp))
        GradientButton(text = "Grant permissions", onClick = { request() })
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        ) { Text("Open app settings") }

        Spacer(Modifier.height(24.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                Text("Offline by design", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your library, playlists and listening history stay on this device. Nothing " +
                        "is uploaded. Online catalogues are only ever used when a licensed " +
                        "provider is configured at build time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    permission: String,
    granted: Boolean,
    reason: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = NeonPurple)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        permission,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    contentDescription = if (granted) "Granted" else "Not granted",
                    tint = if (granted) RoxifySuccess else RoxifyError
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
