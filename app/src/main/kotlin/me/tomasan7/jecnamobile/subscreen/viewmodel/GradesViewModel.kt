package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.GradesRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val gradesRepository: GradesRepository
) : ViewModel()
{
    var uiState by mutableStateOf(GradesState())
        private set

    private var loadGradesJob: Job? = null

    init
    {
        loadGrades()
    }

    fun selectSchoolYearHalf(schoolYearHalf: SchoolYearHalf)
    {
        uiState = uiState.copy(selectedSchoolYearHalf = schoolYearHalf)
        loadGrades()
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        uiState = uiState.copy(selectedSchoolYear = schoolYear)
        loadGrades()
    }

    fun loadGrades()
    {
        uiState = uiState.copy(loading = true)

        loadGradesJob?.cancel()

        loadGradesJob = viewModelScope.launch {
            val grades = gradesRepository.queryGradesPage(uiState.selectedSchoolYear, uiState.selectedSchoolYearHalf)

            uiState = uiState.copy(loading = false, gradesPage = grades)
        }
    }
}