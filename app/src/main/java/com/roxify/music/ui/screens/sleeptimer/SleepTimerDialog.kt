package com.roxify.music.ui.screens.sleeptimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roxify.music.util.Formatters

private val options = listOf(5, 10, 15, 30, 45, 60)

/** Sleep timer picker. When a timer is already running it offers to cancel it. */
@Composable
fun SleepTimerDialog(
    remainingMs: Long,
    onDismiss: () -> Unit,
    onStart: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var selected by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep timer") },
        text = {
            Column {
                if (remainingMs > 0L) {
                    Text(
                        text = "Stopping in ${Formatters.duration(remainingMs)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = "Playback will pause automatically after the selected time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { minutes ->
                                FilterChip(
                                    selected = selected == minutes,
                                    onClick = { selected = minutes },
                                    label = { Text("$minutes min") }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { onStart(selected) }) { Text("Start") }
        },
        dismissButton = {
            if (remainingMs > 0L) {
                TextButton(onClick = onCancel) { Text("Cancel timer") }
            } else {
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    )
}
