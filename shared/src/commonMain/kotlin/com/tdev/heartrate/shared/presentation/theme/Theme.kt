package com.tdev.heartrate.shared.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryRedLight,
    onPrimaryContainer = TextDarkCharcoal,
    secondary = TextGray,
    onSecondary = SurfaceWhite,
    background = BackgroundWhite,
    onBackground = TextDarkCharcoal,
    surface = SurfaceWhite,
    onSurface = TextDarkCharcoal,
    error = ErrorRed,
    onError = SurfaceWhite
)

private val DarkColors = darkColorScheme(
    primary = PrimaryRedLight,
    onPrimary = TextDarkCharcoal,
    primaryContainer = PrimaryRedDark,
    onPrimaryContainer = SurfaceWhite,
    secondary = TextGray,
    onSecondary = TextDarkCharcoal,
    background = Color(0xFF121212),
    onBackground = BackgroundWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = BackgroundWhite,
    error = PrimaryRedLight,
    onError = TextDarkCharcoal
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
