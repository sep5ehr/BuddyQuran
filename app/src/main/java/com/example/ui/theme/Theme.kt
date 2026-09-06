package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WarmGold,
    onPrimary = Color(0xFF1B1B1B),
    primaryContainer = EmeraldPrimaryVariant,
    onPrimaryContainer = Color(0xFFE8F3EE),
    secondary = WarmGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2E2615),
    onSecondaryContainer = WarmGoldLight,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDark,
    secondary = WarmGoldDark,
    onSecondary = Color.White,
    secondaryContainer = WarmGoldLight,
    onSecondaryContainer = WarmGoldDark,
    background = ParchmentBackground,
    onBackground = TextPrimaryLight,
    surface = ParchmentSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible wrapper for existing template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    QuranAppTheme(darkTheme = darkTheme, content = content)
}
