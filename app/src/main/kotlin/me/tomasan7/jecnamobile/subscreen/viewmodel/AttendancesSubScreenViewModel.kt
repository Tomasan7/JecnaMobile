package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.AttendancesRepository
import javax.inject.Inject

@HiltViewModel
class AttendancesSubScreenViewModel @Inject constructor(
    private val attendancesRepository: AttendancesRepository
) : ViewModel()
{
    var uiState by mutableStateOf(AttendancesSubScreenState())
        private set

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