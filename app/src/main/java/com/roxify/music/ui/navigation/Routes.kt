package com.roxify.music.ui.navigation

import android.net.Uri

/** All navigation routes in one place. */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // Bottom-navigation tabs
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val PROFILE = "profile"

    // Detail screens
    const val SEARCH_RESULTS = "search_results"
    const val ALBUM = "album"
    const val ARTIST = "artist"
    const val PLAYER = "player"
    const val QUEUE = "queue"
    const val PLAYLIST = "playlist"
    const val FAVORITES = "favorites"
    const val RECENT = "recent"
    const val LOCAL_MUSIC = "local_music"
    const val SETTINGS = "settings"
    const val EQUALIZER = "equalizer"
    const val PERMISSIONS = "permissions"
    const val ABOUT = "about"

    const val ARG_QUERY = "query"
    const val ARG_ALBUM_ID = "albumId"
    const val ARG_ARTIST_ID = "artistId"
    const val ARG_PLAYLIST_ID = "playlistId"

    fun searchResults(query: String): String = "$SEARCH_RESULTS?$ARG_QUERY=${Uri.encode(query)}"
    fun album(albumId: String): String = "$ALBUM/${Uri.encode(albumId)}"
    fun artist(artistId: String): String = "$ARTIST/${Uri.encode(artistId)}"
    fun playlist(playlistId: Long): String = "$PLAYLIST/$playlistId"

    val detailWithQuery = "$SEARCH_RESULTS?$ARG_QUERY={$ARG_QUERY}"
    val detailAlbum = "$ALBUM/{$ARG_ALBUM_ID}"
    val detailArtist = "$ARTIST/{$ARG_ARTIST_ID}"
    val detailPlaylist = "$PLAYLIST/{$ARG_PLAYLIST_ID}"
}
