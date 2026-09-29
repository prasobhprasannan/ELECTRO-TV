package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF031A24),
    primaryContainer = Color(0xFF0C384D),
    onPrimaryContainer = Color(0xFFB8E8FC),
    secondary = ElectricIndigo,
    onSecondary = Color(0xFF13153B),
    secondaryContainer = Color(0xFF262A56),
    onSecondaryContainer = Color(0xFFDDD8FD),
    tertiary = DigitalPurple,
    background = MidnightDarkBg,
    onBackground = TextPrimaryDark,
    surface = MidnightDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = MidnightBorder,
    error = LiveBroadcastRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // IPTV is best enjoyed in modern dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
