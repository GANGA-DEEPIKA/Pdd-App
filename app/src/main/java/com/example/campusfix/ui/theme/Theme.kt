package com.example.campusfix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF1976D2),
    tertiary = Color(0xFF0D47A1),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937)
)

@Composable
fun CampusFixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
