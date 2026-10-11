package com.roxify.music.data.remote

import com.roxify.music.data.local.DemoCatalog
import com.roxify.music.domain.model.Song
import com.roxify.music.domain.model.SongSource

/**
 * Abstraction over an authorised online music catalogue.
 *
 * ROXIFY deliberately ships **without** any bundled streaming credentials and without any
 * scraping of third-party services. A real deployment plugs in a licensed provider
 * (e.g. a Jamendo / Deezer / licensed-CDN client) that implements this interface and is
 * configured with a server-issued token. Until that happens the app runs in
 * [OnlineStatus.NOT_CONFIGURED] mode and only ever shows the clearly-labelled offline
 * demo catalogue — never a fake "real" song.
 */
interface OnlineMusicProvider {
    val id: String
    val displayName: String

    /** True only when a valid, licensed configuration is present. */
    val isConfigured: Boolean

    suspend fun trending(): List<Song>
    suspend fun recommendations(seedGenres: List<String>): List<Song>
    suspend fun search(query: String): List<Song>
}

enum class OnlineStatus {
    NOT_CONFIGURED,
    CONFIGURED,
    ERROR
}

data class OnlineResult(
    val status: OnlineStatus,
    val songs: List<Song> = emptyList(),
    val message: String? = null
)

/**
 * The default provider used while no licensed catalogue is wired up. It returns the
 * bundled demo tracks (already tagged [SongSource.DEMO]) so the discovery surfaces stay
 * functional, and reports itself as not configured so the UI can show an honest banner.
 */
class OfflineDemoProvider : OnlineMusicProvider {
    override val id: String = "roxify-offline-demo"
    override val displayName: String = "ROXIFY Demo Library (offline)"
    override val isConfigured: Boolean = false

    override suspend fun trending(): List<Song> = DemoCatalog.songs

    override suspend fun recommendations(seedGenres: List<String>): List<Song> =
        DemoCatalog.songs.filter { it.genre in seedGenres }.ifEmpty { DemoCatalog.songs }

    override suspend fun search(query: String): List<Song> = DemoCatalog.songs.filter {
        it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
    }
}

/** Thin facade the UI talks to; hides which provider (if any) is active. */
class OnlineMusicRepository(
    private val provider: OnlineMusicProvider = OfflineDemoProvider()
) {
    val providerName: String get() = provider.displayName
    val isConfigured: Boolean get() = provider.isConfigured

    private val notConfiguredMessage: String =
        "No licensed online catalogue is configured. Add a server-issued token to enable " +
            "streaming. Showing the offline demo library instead."

    suspend fun trending(): OnlineResult = runCatching { provider.trending() }
        .fold(
            onSuccess = { OnlineResult(status = status(), songs = it, message = messageOrNull()) },
            onFailure = { OnlineResult(OnlineStatus.ERROR, emptyList(), it.message) }
        )

    suspend fun recommendations(genres: List<String>): OnlineResult =
        runCatching { provider.recommendations(genres) }.fold(
            onSuccess = { OnlineResult(status = status(), songs = it, message = messageOrNull()) },
            onFailure = { OnlineResult(OnlineStatus.ERROR, emptyList(), it.message) }
        )

    suspend fun search(query: String): OnlineResult = runCatching { provider.search(query) }
        .fold(
            onSuccess = { OnlineResult(status = status(), songs = it, message = messageOrNull()) },
            onFailure = { OnlineResult(OnlineStatus.ERROR, emptyList(), it.message) }
        )

    private fun status(): OnlineStatus =
        if (provider.isConfigured) OnlineStatus.CONFIGURED else OnlineStatus.NOT_CONFIGURED

    private fun messageOrNull(): String? = if (provider.isConfigured) null else notConfiguredMessage
}
