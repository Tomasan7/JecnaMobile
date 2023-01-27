package me.tomasan7.jecnamobile.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun <T> rememberMutableStateOf(value: T) = remember { mutableStateOf(value) }

fun Color.manipulate(factor: Float): Color
{
    val r = (red * factor).coerceIn(0f, 1f)
    val g = (green * factor).coerceIn(0f, 1f)
    val b = (blue * factor).coerceIn(0f, 1f)

    return Color(r, g, b, alpha)
}

@Composable
fun isAppInDarkTheme(settings: Settings) = when(settings.theme)
{
    Theme.DARK -> true
    Theme.LIGHT -> false
    Theme.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun isAppInDarkTheme() = isAppInDarkTheme(settings = awaitSettings())