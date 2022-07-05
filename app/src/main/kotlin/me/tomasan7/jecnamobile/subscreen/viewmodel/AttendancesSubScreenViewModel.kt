package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.AttendancesRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AttendancesSubScreenViewModel @Inject constructor(
    private val attendancesRepository: AttendancesRepository
) : ViewModel()
{
    var uiState by mutableStateOf(AttendancesSubScreenState(selectedMonth = LocalDate.now().month, selectedSchoolYear = SchoolYear(LocalDate.now())))
        private set

    init
    {
        loadAttendances()
    }

    fun selectMonth(month: Month)
    {
        uiState = uiState.copy(selectedMonth = month)
        loadAttendances()
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        uiState = uiState.copy(selectedSchoolYear = schoolYear)
        loadAttendances()
    }

    fun loadAttendances()
    {
        uiState = uiState.copy(loading = true)

        viewModelScope.launch {
            val attendances = attendancesRepository.queryAttendancesPage(uiState.selectedSchoolYear, uiState.selectedMonth.value)

            uiState = uiState.copy(loading = false, attendancesPage = attendances)
        }
    }
}