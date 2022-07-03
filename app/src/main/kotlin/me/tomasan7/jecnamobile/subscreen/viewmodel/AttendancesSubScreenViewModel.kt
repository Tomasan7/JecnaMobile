package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.RepositoryContainer

class AttendancesSubScreenViewModel : ViewModel()
{

    var uiState by mutableStateOf(AttendancesSubScreenState())
        private set

    private val attendancesRepository = RepositoryContainer.attendancesRepository

    init
    {
        loadAttendances()
    }

    fun loadAttendances()
    {
        uiState = uiState.copy(loading = true)

        viewModelScope.launch {
            val attendances = attendancesRepository.queryAttendancesPage()

            val attendanceRows = mutableListOf<AttendanceRow>()

            for (day in attendances.days)
                attendanceRows.add(AttendanceRow(day, attendances[day].map(Any::toString)))

            uiState = uiState.copy(loading = false, attendanceRows = attendanceRows)
        }
    }
}