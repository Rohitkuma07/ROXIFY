package com.roxify.music

import com.roxify.music.data.remote.OfflineDemoProvider
import com.roxify.music.data.remote.OnlineMusicRepository
import com.roxify.music.data.remote.OnlineStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for the online facade while no licensed provider is configured:
 * it must never pretend to stream real songs and must label everything as demo.
 */
class OnlineMusicTest {

    private val repository = OnlineMusicRepository()

    @Test
    fun `default provider is not configured and reports it`() {
        assertFalse(repository.isConfigured)
        assertEquals("ROXIFY Demo Library (offline)", repository.providerName)
    }

    @Test
    fun `trending returns NOT_CONFIGURED with demo songs only`() = runTest {
        val result = repository.trending()
        assertEquals(OnlineStatus.NOT_CONFIGURED, result.status)
        assertTrue(result.songs.isNotEmpty())
        assertTrue(result.songs.all { it.isDemo })
        assertTrue(result.message?.contains("token", ignoreCase = true) == true)
    }

    @Test
    fun `search filters demo catalogue by query`() = runTest {
        val result = repository.search("velvet")
        assertEquals(OnlineStatus.NOT_CONFIGURED, result.status)
        assertTrue(result.songs.all { it.title.contains("Velvet", ignoreCase = true) })
    }

    @Test
    fun `provider search is case insensitive`() = runTest {
        val provider = OfflineDemoProvider()
        assertTrue(provider.search("MIDNIGHT").isNotEmpty())
        assertTrue(provider.search("zzz-not-there").isEmpty())
    }
}