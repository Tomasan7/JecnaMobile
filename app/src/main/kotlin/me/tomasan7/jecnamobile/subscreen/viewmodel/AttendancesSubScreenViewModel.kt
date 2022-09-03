package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.AttendancesRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.R
import java.time.Month
import javax.inject.Inject

@HiltViewModel
class AttendancesSubScreenViewModel @Inject constructor(
    private val attendancesRepository: AttendancesRepository,
    @ApplicationContext
    private val appContext: Context
) : ViewModel()
{
    var uiState by mutableStateOf(AttendancesSubScreenState())
        private set

    private var loadAttendancesJob: Job? = null

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

        loadAttendancesJob?.cancel()

        loadAttendancesJob = viewModelScope.launch {
            try
            {
                val attendances = attendancesRepository.queryAttendancesPage(uiState.selectedSchoolYear, uiState.selectedMonth.value)
                uiState = uiState.copy(loading = false, attendancesPage = attendances)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.attendance_load_error), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
        }
    }
}