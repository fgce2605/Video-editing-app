package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
    primary = StudioRedPrimary,
    onPrimary = StudioTextPrimary,
    secondary = StudioBlueSecondary,
    onSecondary = StudioTextPrimary,
    tertiary = StudioCyanAI,
    onTertiary = StudioDarkBg,
    background = StudioDarkBg,
    onBackground = StudioTextPrimary,
    surface = StudioCardBg,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioTrackBg,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder
)

@Composable
fun ProEditStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}

