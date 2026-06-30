package com.tradecalc.netpl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    secondary = AmberAccent,
    background = SurfaceLight,
    surface = CardLight,
    onSurface = OnSurfaceLight,
    error = RedLoss
)

private val DarkColors = darkColorScheme(
    primary = AmberAccent,
    onPrimary = Color.Black,
    secondary = NavyPrimary,
    error = RedLoss
)

@Composable
fun NetPLCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
