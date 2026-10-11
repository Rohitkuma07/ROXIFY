package com.roxify.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roxify.music.data.prefs.ThemeMode

private val RoxifyDarkScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = RoxifyBlack,
    primaryContainer = NeonViolet,
    onPrimaryContainer = RoxifyOnSurface,
    secondary = NeonPink,
    onSecondary = RoxifyBlack,
    tertiary = NeonBlue,
    background = RoxifyBlack,
    onBackground = RoxifyOnSurface,
    surface = RoxifySurface,
    onSurface = RoxifyOnSurface,
    surfaceVariant = RoxifySurfaceHigh,
    onSurfaceVariant = RoxifyOnSurfaceVariant,
    outline = RoxifyOutline,
    error = RoxifyError,
    onError = RoxifyBlack
)

private val LightSurfaceVariant = Color(0xFFEAE2F7)
private val LightOnSurfaceVariant = Color(0xFF5B4A7A)
private val LightOutline = Color(0xFFC9BCE4)

private val RoxifyLightScheme = lightColorScheme(
    primary = NeonViolet,
    onPrimary = RoxifyOnSurface,
    secondary = NeonPink,
    onSecondary = RoxifyOnSurface,
    tertiary = NeonBlue,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

val RoxifyTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

val RoxifyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun RoxifyTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    // Dynamic colour is offered in Settings but the ROXIFY brand palette is the default
    // so the identity stays consistent; when enabled we still keep the brand primary.
    val scheme = if (dark) RoxifyDarkScheme else RoxifyLightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = RoxifyTypography,
        shapes = RoxifyShapes,
        content = content
    )
}
