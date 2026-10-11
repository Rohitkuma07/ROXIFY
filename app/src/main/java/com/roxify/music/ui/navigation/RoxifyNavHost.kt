package com.roxify.music.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.screens.about.AboutScreen
import com.roxify.music.ui.screens.album.AlbumDetailScreen
import com.roxify.music.ui.screens.artist.ArtistDetailScreen
import com.roxify.music.ui.screens.equalizer.EqualizerScreen
import com.roxify.music.ui.screens.favorites.FavoritesScreen
import com.roxify.music.ui.screens.home.HomeScreen
import com.roxify.music.ui.screens.library.LibraryScreen
import com.roxify.music.ui.screens.local.LocalMusicScreen
import com.roxify.music.ui.screens.onboarding.OnboardingScreen
import com.roxify.music.ui.screens.permissions.PermissionsScreen
import com.roxify.music.ui.screens.player.PlayerScreen
import com.roxify.music.ui.screens.playlist.PlaylistDetailScreen
import com.roxify.music.ui.screens.profile.ProfileScreen
import com.roxify.music.ui.screens.queue.QueueScreen
import com.roxify.music.ui.screens.recent.RecentlyPlayedScreen
import com.roxify.music.ui.screens.search.SearchResultsScreen
import com.roxify.music.ui.screens.search.SearchScreen
import com.roxify.music.ui.screens.settings.SettingsScreen
import com.roxify.music.ui.screens.splash.SplashScreen

/**
 * The complete ROXIFY navigation graph. Registers all 18 screens and the shared bottom
 * navigation destinations.
 */
@Composable
fun RoxifyNavHost(
    nav: NavHostController,
    container: AppContainer,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onFinishOnboarding: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        NavHost(navController = nav, startDestination = Routes.SPLASH) {

            composable(Routes.SPLASH) {
                SplashScreen(
                    onFinished = {
                        val target = if (settings.onboardingComplete) Routes.HOME else Routes.ONBOARDING
                        nav.navigate(target) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        onFinishOnboarding()
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // ------------------------------------------------------------ main tabs
            composable(Routes.HOME) { HomeScreen(nav, container, settings) }
            composable(Routes.SEARCH) { SearchScreen(nav, container) }
            composable(Routes.LIBRARY) { LibraryScreen(nav, container, settings) }
            composable(Routes.PROFILE) { ProfileScreen(nav, container, settings) }

            // ------------------------------------------------------------ discovery
            composable(
                route = Routes.detailWithQuery,
                arguments = listOf(navArgument(Routes.ARG_QUERY) { type = NavType.StringType })
            ) { entry ->
                SearchResultsScreen(
                    nav = nav,
                    container = container,
                    query = entry.arguments?.getString(Routes.ARG_QUERY).orEmpty()
                )
            }

            composable(
                route = Routes.detailAlbum,
                arguments = listOf(navArgument(Routes.ARG_ALBUM_ID) { type = NavType.StringType })
            ) { entry ->
                AlbumDetailScreen(
                    nav = nav,
                    container = container,
                    albumId = entry.arguments?.getString(Routes.ARG_ALBUM_ID).orEmpty()
                )
            }

            composable(
                route = Routes.detailArtist,
                arguments = listOf(navArgument(Routes.ARG_ARTIST_ID) { type = NavType.StringType })
            ) { entry ->
                ArtistDetailScreen(
                    nav = nav,
                    container = container,
                    artistId = entry.arguments?.getString(Routes.ARG_ARTIST_ID).orEmpty()
                )
            }

            // ------------------------------------------------------------ playback
            composable(Routes.PLAYER) { PlayerScreen(nav, container) }
            composable(Routes.QUEUE) { QueueScreen(nav, container) }

            // ------------------------------------------------------------ library
            composable(
                route = Routes.detailPlaylist,
                arguments = listOf(navArgument(Routes.ARG_PLAYLIST_ID) { type = NavType.LongType })
            ) { entry ->
                PlaylistDetailScreen(
                    nav = nav,
                    container = container,
                    playlistId = entry.arguments?.getLong(Routes.ARG_PLAYLIST_ID) ?: 0L
                )
            }
            composable(Routes.FAVORITES) { FavoritesScreen(nav, container) }
            composable(Routes.RECENT) { RecentlyPlayedScreen(nav, container) }
            composable(Routes.LOCAL_MUSIC) { LocalMusicScreen(nav, container) }

            // ------------------------------------------------------------ settings
            composable(Routes.SETTINGS) { SettingsScreen(nav, container, settings) }
            composable(Routes.EQUALIZER) { EqualizerScreen(nav, container) }
            composable(Routes.PERMISSIONS) { PermissionsScreen(nav, container) }
            composable(Routes.ABOUT) { AboutScreen(nav, container) }
        }
    }
}
