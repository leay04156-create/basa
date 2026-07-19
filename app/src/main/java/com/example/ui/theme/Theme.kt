package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = LuxuryDarkBlue,
    secondary = LuxuryGoldLight,
    onSecondary = LuxuryDarkBlue,
    tertiary = LuxuryGoldDark,
    background = LuxuryDarkBlue,
    onBackground = LuxuryWhite,
    surface = LuxuryDarkBlueAlt,
    onSurface = LuxuryWhite,
    surfaceVariant = LuxuryDarkBlue,
    onSurfaceVariant = LuxuryGoldLight,
    outline = LuxuryGoldDark
)

private val LightColorScheme = lightColorScheme(
    primary = LuxuryGoldDark,
    onPrimary = LuxuryWhite,
    secondary = LuxuryGold,
    onSecondary = LuxuryDarkBlue,
    tertiary = LuxuryGoldDark,
    background = LuxuryCreamBg,
    onBackground = LuxuryTextDark,
    surface = LuxuryWhite,
    onSurface = LuxuryTextDark,
    surfaceVariant = LuxuryCreamBg,
    onSurfaceVariant = LuxuryTextMuted,
    outline = LuxuryGoldLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor to enforce the custom designed, highly curated luxury hotel palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
