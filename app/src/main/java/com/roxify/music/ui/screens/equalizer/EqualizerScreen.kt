package com.roxify.music.ui.screens.equalizer

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roxify.music.di.AppContainer
import com.roxify.music.playback.EqBand
import com.roxify.music.ui.components.GlassCard
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.theme.NeonPurple

/**
 * Hardware equalizer. Android exposes the platform [android.media.audiofx.Equalizer]; support
 * depends entirely on the device, so the screen degrades gracefully when unavailable.
 */
@Composable
fun EqualizerScreen(nav: NavController, container: AppContainer) {
    val controller = container.equalizerController
    val state by controller.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { controller.probeSupport() }

    Column(Modifier.fillMaxSize()) {
        RoxifyTopBar(
            title = "Equalizer",
            subtitle = if (state.supported) "Device effect available" else "Not supported on this device",
            onBack = { nav.popBackStack() }
        )

        if (!state.supported) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Equalizer,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Equalizer unavailable",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "This device does not expose a system equalizer effect to apps. Your music " +
                        "still plays normally with the built-in player.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Enable equalizer", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Applies to audio played through ROXIFY",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { controller.setEnabled(it) }
                    )
                }
            }

            if (state.presets.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Presets", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.presets.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { _, name ->
                                val index = state.presets.indexOf(name).toShort()
                                FilterChip(
                                    selected = state.currentPreset == index.toInt(),
                                    onClick = { controller.usePreset(index) },
                                    label = { Text(name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Bands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            state.bands.forEach { band ->
                BandSlider(
                    band = band,
                    enabled = state.enabled,
                    onChange = { controller.setBandLevel(band.index, it) }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Bands are provided by your device's audio effect. Values are shown in dB " +
                    "relative to the device range.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun BandSlider(band: EqBand, enabled: Boolean, onChange: (Short) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${band.centerFrequencyHz} Hz",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${band.levelMb / 100} dB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = band.levelMb.toFloat(),
            onValueChange = { onChange(it.toInt().toShort()) },
            valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
            enabled = enabled
        )
    }
}
