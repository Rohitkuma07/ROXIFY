package com.roxify.music

import com.roxify.music.domain.model.Mood
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSource
import com.roxify.music.playback.PlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** State-model tests: progress maths and flags the player UI depends on. */
class PlayerUiStateTest {

    private fun song(id: String): Song = Song(
        id = id, title = "T", artist = "A", uri = "asset:///demo/demo_chill.wav",
        source = SongSource.DEMO
    )

    @Test
    fun `progress clamps to zero and one`() {
        assertEquals(0f, PlayerUiState().progress, 0.001f)
        val half = PlayerUiState(positionMs = 50L, durationMs = 100L)
        assertEquals(0.5f, half.progress, 0.001f)
        val over = PlayerUiState(positionMs = 500L, durationMs = 100L)
        assertEquals(1f, over.progress, 0.001f)
    }

    @Test
    fun `duration fallback and hasSong`() {
        val withSong = PlayerUiState(currentSong = song("1"))
        assertTrue(withSong.hasSong)
        assertFalse(PlayerUiState().hasSong)
    }

    @Test
    fun `sleep timer active only while remaining time positive`() {
        assertFalse(PlayerUiState().isSleepTimerActive)
        assertTrue(PlayerUiState(sleepTimerRemainingMs = 1000L).isSleepTimerActive)
    }

    @Test
    fun `mood ids round trip`() {
        assertEquals(Mood.ROMANTIC, Mood.fromId("romantic"))
        assertEquals(Mood.LOFI, Mood.fromId("lofi"))
        assertEquals(Mood.PARTY, Mood.fromId("PARTY"))
        assertTrue(Mood.fromId("nope") == null)
    }
}