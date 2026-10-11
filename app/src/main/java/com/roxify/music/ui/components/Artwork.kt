package com.roxify.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.roxify.music.ui.theme.ArtworkGradients
import kotlin.math.absoluteValue

/**
 * Album/track artwork. Uses the real image when available (device or online artwork)
 * and otherwise falls back to a stable, brand-consistent gradient derived from [seed].
 */
@Composable
fun ArtworkBox(
    artworkUri: String?,
    seed: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    iconFraction: Float = 0.42f
) {
    val gradient = remember(seed) {
        ArtworkGradients[seed.hashCode().absoluteValue % ArtworkGradients.size]
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (!artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxSize(iconFraction)
            )
        }
    }
}
