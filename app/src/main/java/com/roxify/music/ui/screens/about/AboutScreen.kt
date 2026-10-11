package com.roxify.music.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.roxify.music.BuildConfig
import com.roxify.music.di.AppContainer
import com.roxify.music.ui.components.GlassCard
import com.roxify.music.ui.components.RoxifyTopBar
import com.roxify.music.ui.theme.RoxifyGradient

/** About screen: identity, version, licensing honesty and tech credits. */
@Composable
fun AboutScreen(nav: NavController, container: AppContainer) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        RoxifyTopBar(title = "About", onBack = { nav.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(RoxifyGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "ROXIFY",
                style = MaterialTheme.typography.headlineSmall,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Feel Every Beat",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20) {
                Column {
                    Text("Offline first", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "ROXIFY scans the audio on your device and plays it completely offline. " +
                            "Your library, favourites, playlists and history never leave this phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20) {
                Column {
                    Text("Demo & online content", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "The bundled demo tracks are original, royalty-free tones generated for " +
                            "ROXIFY and are always labelled DEMO. Real streaming requires a " +
                            "licensed catalogue: connect one at build time via a server-issued " +
                            "token (see README). Current online provider: " +
                            container.onlineMusicRepository.providerName +
                            if (container.onlineMusicRepository.isConfigured) "" else " (not configured).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20) {
                Column {
                    Text("Built with", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Kotlin · Jetpack Compose · Media3 ExoPlayer · Room · DataStore · " +
                            "Coil · Navigation. Premium black & neon purple theme with " +
                            "glassmorphism.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "ROXIFY is a personal project. No streaming data is harvested or sold.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}