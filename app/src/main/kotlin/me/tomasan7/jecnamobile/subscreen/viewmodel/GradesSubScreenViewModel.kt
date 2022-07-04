package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.GradesRepository
import javax.inject.Inject

@HiltViewModel
class GradesSubScreenViewModel @Inject constructor(
    private val gradesRepository: GradesRepository
) : ViewModel()
{
    var uiState by mutableStateOf(GradesSubScreenState())
        private set

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