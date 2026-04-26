package com.hamza.icleaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LaundryBlue,
    onPrimary = White,
    primaryContainer = LaundryLightBlue,
    onPrimaryContainer = LaundryBlue,
    
    secondary = LaundryGreen,
    onSecondary = White,
    
    background = SurfaceGray,
    onBackground = LaundryDark,
    
    surface = White,
    onSurface = LaundryDark,
    
    error = LaundryRed,
    onError = White,
    
    outline = LaundryGrey
)

private val DarkColorScheme = darkColorScheme(
    primary = LaundryLightBlue,
    onPrimary = LaundryBlue,
    primaryContainer = LaundryBlue,
    onPrimaryContainer = LaundryLightBlue,
    
    secondary = LaundryGreen,
    onSecondary = White,
    
    background = LaundryDark,
    onBackground = White,
    
    surface = Color(0xFF1C1B1F),
    onSurface = White,
    
    error = LaundryRed,
    onError = White,
    
    outline = LaundryGrey
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
