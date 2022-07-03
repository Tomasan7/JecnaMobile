package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.RepositoryContainer
import java.util.*

class GradesSubScreenViewModel : ViewModel()
{
    var uiState by mutableStateOf(GradesSubScreenState())
        private set

    private val gradesRepository = RepositoryContainer.gradesRepository

    init
    {
        loadGrades()
    }

    fun loadGrades()
    {
        uiState = uiState.copy(loading = true)

        viewModelScope.launch {
            val grades = gradesRepository.queryGradesPage()

            uiState = uiState.copy(loading = false, gradesPage = grades)
        }
    }
}