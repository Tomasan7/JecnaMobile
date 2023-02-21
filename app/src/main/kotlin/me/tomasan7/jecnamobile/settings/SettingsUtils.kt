package me.tomasan7.jecnamobile.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import me.tomasan7.jecnamobile.util.awaitSettings

@Composable
fun isAppInDarkTheme(settings: Settings) = when(settings.theme)
{
    Theme.DARK   -> true
    Theme.LIGHT  -> false
    Theme.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun isAppInDarkTheme() = isAppInDarkTheme(settings = awaitSettings())