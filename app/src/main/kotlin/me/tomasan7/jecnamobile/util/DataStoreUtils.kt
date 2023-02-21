package me.tomasan7.jecnamobile.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnamobile.settings.Settings

val Context.settingsDataStore by dataStore("settings.json", SettingsSerializer)

fun <T> Flow<T>.awaitFirst() = runBlocking { first() }

@Composable
fun settingsAsState() = LocalContext.current.settingsDataStore.data.collectAsState(initial = Settings())

@Composable
fun settingsAsStateAwaitFirst(): State<Settings>
{
    val settings = awaitSettings()
    return LocalContext.current.settingsDataStore.data.collectAsState(initial = settings)
}

@Composable
fun awaitSettings() = LocalContext.current.settingsDataStore.data.awaitFirst()