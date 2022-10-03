package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.util.Theme
import me.tomasan7.jecnamobile.util.settingsDataStore
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext
    val appContext: Context
) : ViewModel()
{
    val settingsDataStore = appContext.settingsDataStore

    fun setTheme(theme: Theme)
    {
        viewModelScope.launch {
            settingsDataStore.updateData {
                it.copy(theme = theme)
            }
        }
    }

    fun setOpenSubScreen(subScreenRoute: String)
    {
        viewModelScope.launch {
            settingsDataStore.updateData {
                it.copy(openSubScreenRoute = subScreenRoute)
            }
        }
    }
}