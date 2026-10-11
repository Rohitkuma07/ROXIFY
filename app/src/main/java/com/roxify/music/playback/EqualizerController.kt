package com.roxify.music.playback

import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EqBand(
    val index: Short,
    val centerFrequencyHz: Int,
    val minLevelMb: Short,
    val maxLevelMb: Short,
    val levelMb: Short
)

data class EqualizerState(
    val supported: Boolean = false,
    val enabled: Boolean = false,
    val bands: List<EqBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1
)

/**
 * Wraps Android's platform [Equalizer] effect. Support varies wildly between devices and
 * OEMs, so every call is guarded and the UI shows "not supported" when the effect cannot
 * be created. Nothing here throws into the UI.
 */
class EqualizerController {

    private var equalizer: Equalizer? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    /** Creates the effect (if possible) without enabling it, so the UI can show bands. */
    fun probeSupport() {
        if (equalizer == null) {
            equalizer = runCatching { Equalizer(0, 0) }.getOrNull()
        }
        refresh()
        _state.update { it.copy(enabled = false) }
    }

    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            if (equalizer == null) equalizer = runCatching { Equalizer(0, 0) }.getOrNull()
            val eq = equalizer
            if (eq == null) {
                _state.update { it.copy(supported = false, enabled = false) }
                return
            }
            runCatching { eq.enabled = true }
            refresh()
        } else {
            runCatching { equalizer?.enabled = false }
            _state.update { it.copy(enabled = false) }
        }
    }

    fun setBandLevel(index: Short, levelMb: Short) {
        runCatching { equalizer?.setBandLevel(index, levelMb) }
        refresh()
    }

    fun usePreset(preset: Short) {
        runCatching { equalizer?.usePreset(preset) }
        refresh()
    }

    private fun refresh() {
        val eq = equalizer
        if (eq == null) {
            _state.value = EqualizerState(supported = false)
            return
        }
        runCatching {
            val range = eq.bandLevelRange
            val bands = (0 until eq.numberOfBands.toInt()).map { i ->
                val band = i.toShort()
                EqBand(
                    index = band,
                    centerFrequencyHz = eq.getCenterFreq(band) / 1000,
                    minLevelMb = range[0],
                    maxLevelMb = range[1],
                    levelMb = eq.getBandLevel(band)
                )
            }
            val presets = (0 until eq.numberOfPresets.toInt()).map {
                eq.getPresetName(it.toShort()).toString()
            }
            _state.value = EqualizerState(
                supported = true,
                enabled = eq.enabled,
                bands = bands,
                presets = presets,
                currentPreset = eq.currentPreset.toInt()
            )
        }.onFailure {
            _state.update { it.copy(supported = false) }
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        equalizer = null
        _state.value = EqualizerState()
    }
}
