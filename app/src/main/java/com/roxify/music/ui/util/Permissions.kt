package com.roxify.music.ui.util

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.roxify.music.util.MediaPermissions

/**
 * Returns a lambda that requests the audio (and, on Android 13+, notification)
 * permissions. [onResult] is called with true when audio access was granted.
 */
@Composable
fun rememberAudioPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onResult(result[MediaPermissions.audioPermission] == true)
    }
    return {
        val permissions = buildList {
            add(MediaPermissions.audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        launcher.launch(permissions)
    }
}

@Composable
fun currentAudioPermissionGranted(): Boolean {
    val context = LocalContext.current
    return MediaPermissions.hasAudioPermission(context)
}
