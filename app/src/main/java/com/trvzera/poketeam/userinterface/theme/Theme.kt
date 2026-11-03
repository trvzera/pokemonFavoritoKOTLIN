package com.trvzera.poketeam.userinterface.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoresPoketeam = lightColorScheme(
    primary = Color(0xFFD93645),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBE5E8),
    onPrimaryContainer = Color(0xFF8F2030),
    secondary = Color(0xFFB62C3D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBE5E8),
    onSecondaryContainer = Color(0xFF8F2030),
    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF202B40),
    surface = Color.White,
    onSurface = Color(0xFF202B40),
    surfaceVariant = Color(0xFFEDF1F7),
    onSurfaceVariant = Color(0xFF526079),
    outline = Color(0xFF718097),
    outlineVariant = Color(0xFFDCE3EF)
)

@Composable
fun PoketeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CoresPoketeam, content = content)
}
