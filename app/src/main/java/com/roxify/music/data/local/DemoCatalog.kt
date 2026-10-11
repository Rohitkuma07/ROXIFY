package com.roxify.music.data.local

import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSource

/**
 * Bundled demo catalogue.
 *
 * Every entry here is explicitly flagged with [SongSource.DEMO] and is rendered with a
 * visible "DEMO" badge in the UI. The audio files are short, original royalty-free
 * tones generated specifically for ROXIFY and shipped inside the APK under
 * `assets/demo/`. Nothing here pretends to be a real licensed recording.
 */
object DemoCatalog {

    const val ARTIST_NAME = "ROXIFY Studio"
    private const val ALBUM = "Neon Sessions (Demo)"
    private const val DURATION_MS = 16_000L

    private fun track(
        index: Int,
        title: String,
        mood: Mood,
        file: String
    ): Song = Song(
        id = "demo:$index",
        title = title,
        artist = "$ARTIST_NAME • ${mood.label}",
        album = ALBUM,
        durationMs = DURATION_MS,
        uri = "asset:///demo/$file",
        artworkUri = null,
        source = SongSource.DEMO,
        genre = mood.label,
        dateAdded = 0L,
        trackNumber = index
    )

    val songs: List<Song> = listOf(
        track(1, "Midnight Velvet (Demo)", Mood.ROMANTIC, "demo_romantic.wav"),
        track(2, "Slow Motion Heart (Demo)", Mood.ROMANTIC, "demo_romantic.wav"),
        track(3, "Rain On Glass (Demo)", Mood.SAD, "demo_sad.wav"),
        track(4, "Empty Streetlights (Demo)", Mood.SAD, "demo_sad.wav"),
        track(5, "Purple Haze Lounge (Demo)", Mood.CHILL, "demo_chill.wav"),
        track(6, "Cloud Drift (Demo)", Mood.CHILL, "demo_chill.wav"),
        track(7, "Neon Drop (Demo)", Mood.PARTY, "demo_party.wav"),
        track(8, "Roxify Anthem (Demo)", Mood.PARTY, "demo_party.wav"),
        track(9, "Pulse Runner (Demo)", Mood.WORKOUT, "demo_workout.wav"),
        track(10, "Bass Sprint (Demo)", Mood.WORKOUT, "demo_workout.wav"),
        track(11, "Tape Loop 01 (Demo)", Mood.LOFI, "demo_lofi.wav"),
        track(12, "Dusty Keys (Demo)", Mood.LOFI, "demo_lofi.wav")
    )

    fun songsForMood(mood: Mood): List<Song> = songs.filter { it.genre == mood.label }
}
