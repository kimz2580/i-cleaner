package com.hamza.icleaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = White,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = White,
    
    secondary = SuccessGreen,
    onSecondary = White,
    
    background = BackgroundLight,
    onBackground = TextDark,
    
    surface = White,
    onSurface = TextDark,
    
    error = DangerRed,
    onError = White,
    
    outline = TextMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = White,
    primaryContainer = DeepNavy,
    onPrimaryContainer = White,
    
    secondary = SuccessGreen,
    onSecondary = White,
    
    background = TextDark,
    onBackground = BackgroundLight,
    
    surface = Color(0xFF1E293B),
    onSurface = White,
    
    error = DangerRed,
    onError = White,
    
    outline = TextMuted
)

@Composable
fun ICleanerTheme(
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
