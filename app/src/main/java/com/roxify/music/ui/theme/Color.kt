package com.roxify.music.ui.theme

import androidx.compose.ui.graphics.Color

// ── ROXIFY palette — premium black + neon purple ────────────────────────────────
val NeonViolet = Color(0xFF7B2FF7)
val NeonPurple = Color(0xFF9D4EDD)
val NeonPink = Color(0xFFF72585)
val NeonBlue = Color(0xFF4361EE)

val RoxifyBlack = Color(0xFF05030A)
val RoxifySurface = Color(0xFF120B22)
val RoxifySurfaceHigh = Color(0xFF1C1230)
val RoxifySurfaceGlass = Color(0x14FFFFFF)
val RoxifyOutline = Color(0xFF34244E)

val RoxifyOnSurface = Color(0xFFF4F0FF)
val RoxifyOnSurfaceVariant = Color(0xFFB6A5D6)

val RoxifySuccess = Color(0xFF31D0AA)
val RoxifyError = Color(0xFFFF5A6E)
val RoxifyWarning = Color(0xFFFFC34D)

// Light palette (used only when the user explicitly picks Light mode)
val LightBackground = Color(0xFFF7F4FD)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1030)

val RoxifyGradient = listOf(NeonViolet, NeonPurple, NeonPink)

/** Deterministic gradient used for track/album artwork placeholders. */
val ArtworkGradients: List<List<Color>> = listOf(
    listOf(Color(0xFF7B2FF7), Color(0xFFF72585)),
    listOf(Color(0xFF4361EE), Color(0xFF9D4EDD)),
    listOf(Color(0xFF00C2A8), Color(0xFF4361EE)),
    listOf(Color(0xFFF72585), Color(0xFFFF8C42)),
    listOf(Color(0xFF9D4EDD), Color(0xFF00C2A8)),
    listOf(Color(0xFF3A0CA3), Color(0xFF7209B7)),
    listOf(Color(0xFF7209B7), Color(0xFFF72585)),
    listOf(Color(0xFF2D00F7), Color(0xFF9D4EDD))
)
