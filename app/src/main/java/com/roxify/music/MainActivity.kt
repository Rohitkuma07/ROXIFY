package com.roxify.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roxify.music.data.prefs.AppSettings
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.MiniPlayer
import com.roxify.music.ui.navigation.Routes
import com.roxify.music.ui.navigation.RoxifyBottomBar
import com.roxify.music.ui.navigation.RoxifyNavHost
import com.roxify.music.ui.theme.RoxifyTheme
import kotlinx.coroutines.launch

/**
 * Single-activity host. Collects the persisted [AppSettings] and applies the ROXIFY theme,
 * then renders the navigation graph with the shared mini-player + bottom navigation.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as RoxifyApp).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            RoxifyTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.useDynamicColor
            ) {
                RoxifyRoot(container = container, settings = settings)
            }
        }
    }
}

private val tabRoutes = setOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.PROFILE)
private val fullScreenRoutes = setOf(Routes.SPLASH, Routes.ONBOARDING, Routes.PLAYER)

@Composable
private fun RoxifyRoot(container: AppContainer, settings: AppSettings) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val playerState by container.playbackManager.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in tabRoutes
    val showMiniPlayer = playerState.currentSong != null && currentRoute != null &&
        currentRoute !in fullScreenRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar || showMiniPlayer) {
                Column {
                    if (showMiniPlayer) {
                        MiniPlayer(
                            state = playerState,
                            onExpand = {
                                navController.navigate(Routes.PLAYER) { launchSingleTop = true }
                            },
                            onPlayPause = { container.playbackManager.togglePlayPause() },
                            onNext = { container.playbackManager.next() }
                        )
                    }
                    if (showBottomBar) {
                        RoxifyBottomBar(
                            currentRoute = currentRoute,
                            onSelect = { route ->
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        RoxifyNavHost(
            nav = navController,
            container = container,
            settings = settings,
            contentPadding = innerPadding,
            onFinishOnboarding = {
                scope.launch { container.settingsRepository.setOnboardingComplete(true) }
            }
        )
    }
}
