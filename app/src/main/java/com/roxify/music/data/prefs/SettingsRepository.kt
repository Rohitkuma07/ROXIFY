package com.roxify.music.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.roxify.music.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, DARK, LIGHT }

enum class AudioQuality(val label: String) {
    AUTO("Auto (recommended)"),
    LOW("Low (saves data)"),
    NORMAL("Normal"),
    HIGH("High")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val useDynamicColor: Boolean = false,
    val userName: String = "Music Lover",
    val userImageUri: String? = null,
    val audioQuality: AudioQuality = AudioQuality.AUTO,
    val crossfadeSeconds: Int = 0,
    val gaplessPlayback: Boolean = true,
    val defaultShuffle: Boolean = false,
    val defaultRepeat: RepeatMode = RepeatMode.OFF,
    val onboardingComplete: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val equalizerEnabled: Boolean = false,
    val sleepTimerMinutes: Int = 30,
    val showDemoContent: Boolean = true
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "roxify_settings"
)

/** Persists user preferences with DataStore. Exposes a single [settings] flow. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_IMAGE = stringPreferencesKey("user_image")
        val QUALITY = stringPreferencesKey("audio_quality")
        val CROSSFADE = intPreferencesKey("crossfade_seconds")
        val GAPLESS = booleanPreferencesKey("gapless")
        val DEFAULT_SHUFFLE = booleanPreferencesKey("default_shuffle")
        val DEFAULT_REPEAT = stringPreferencesKey("default_repeat")
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")
        val EQUALIZER = booleanPreferencesKey("equalizer_enabled")
        val SLEEP_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val SHOW_DEMO = booleanPreferencesKey("show_demo_content")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            useDynamicColor = prefs[Keys.DYNAMIC] ?: false,
            userName = prefs[Keys.USER_NAME] ?: "Music Lover",
            userImageUri = prefs[Keys.USER_IMAGE]?.takeIf { it.isNotBlank() },
            audioQuality = prefs[Keys.QUALITY]
                ?.let { runCatching { AudioQuality.valueOf(it) }.getOrNull() }
                ?: AudioQuality.AUTO,
            crossfadeSeconds = prefs[Keys.CROSSFADE] ?: 0,
            gaplessPlayback = prefs[Keys.GAPLESS] ?: true,
            defaultShuffle = prefs[Keys.DEFAULT_SHUFFLE] ?: false,
            defaultRepeat = prefs[Keys.DEFAULT_REPEAT]
                ?.let { runCatching { RepeatMode.valueOf(it) }.getOrNull() }
                ?: RepeatMode.OFF,
            onboardingComplete = prefs[Keys.ONBOARDING] ?: false,
            recentSearches = prefs[Keys.RECENT_SEARCHES]?.toList() ?: emptyList(),
            equalizerEnabled = prefs[Keys.EQUALIZER] ?: false,
            sleepTimerMinutes = prefs[Keys.SLEEP_MINUTES] ?: 30,
            showDemoContent = prefs[Keys.SHOW_DEMO] ?: true
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    suspend fun setUserName(name: String) = edit { it[Keys.USER_NAME] = name }
    suspend fun setUserImage(uri: String?) = edit {
        if (uri.isNullOrBlank()) it.remove(Keys.USER_IMAGE) else it[Keys.USER_IMAGE] = uri
    }
    suspend fun setAudioQuality(quality: AudioQuality) = edit { it[Keys.QUALITY] = quality.name }
    suspend fun setCrossfade(seconds: Int) = edit { it[Keys.CROSSFADE] = seconds.coerceIn(0, 12) }
    suspend fun setGapless(enabled: Boolean) = edit { it[Keys.GAPLESS] = enabled }
    suspend fun setDefaultShuffle(enabled: Boolean) = edit { it[Keys.DEFAULT_SHUFFLE] = enabled }
    suspend fun setDefaultRepeat(mode: RepeatMode) = edit { it[Keys.DEFAULT_REPEAT] = mode.name }
    suspend fun setOnboardingComplete(complete: Boolean) = edit { it[Keys.ONBOARDING] = complete }
    suspend fun setEqualizerEnabled(enabled: Boolean) = edit { it[Keys.EQUALIZER] = enabled }
    suspend fun setSleepTimerMinutes(minutes: Int) = edit { it[Keys.SLEEP_MINUTES] = minutes }
    suspend fun setShowDemoContent(show: Boolean) = edit { it[Keys.SHOW_DEMO] = show }

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        edit { prefs ->
            val existing = prefs[Keys.RECENT_SEARCHES]?.toMutableList() ?: mutableListOf()
            existing.remove(trimmed)
            existing.add(0, trimmed)
            prefs[Keys.RECENT_SEARCHES] = existing.take(10).toSet()
        }
    }

    suspend fun clearRecentSearches() = edit { it.remove(Keys.RECENT_SEARCHES) }

    /** Wipes every stored preference back to defaults (used by "Reset preferences"). */
    suspend fun resetPreferences() = edit { it.clear() }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsDataStore.edit(block)
    }
}
