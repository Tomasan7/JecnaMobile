package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.WebAttendancesRepository
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.screen.viewmodel.LoginScreenViewModel

class AttendancesSubScreenViewModel(application: Application,
                                    jecnaWebClient: JecnaWebClient?) : AndroidViewModel(application)
{
    private val appContext
        get() = getApplication<Application>().applicationContext

    private val authPreferences
        get() = appContext.getSharedPreferences(LoginScreenViewModel.AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(AttendancesSubScreenState())
        private set

    private lateinit var attendancesRepository: WebAttendancesRepository

    init
    {
        if (jecnaWebClient == null)
        {
            viewModelScope.launch {
                val client = JecnaWebClient(authPreferences.getString(LoginScreenViewModel.PREFERENCES_USERNAME_KEY, null) as String,
                                            authPreferences.getString(LoginScreenViewModel.PREFERENCES_PASSWORD_KEY, null) as String)
                client.login()
                attendancesRepository = WebAttendancesRepository(client)
                loadAttendances()
            }
        }
        else
        {
            attendancesRepository = WebAttendancesRepository(jecnaWebClient)
            loadAttendances()
        }
    }

    fun loadAttendances()
    {
        uiState = uiState.copy(loading = true)

        viewModelScope.launch {
            val attendances = attendancesRepository.queryAttendances()

            val attendanceRows = mutableListOf<AttendanceRow>()

            for (day in attendances.days)
                attendanceRows.add(AttendanceRow(day, attendances.getAttendancesForDay(day).map(Any::toString)))

            uiState = uiState.copy(loading = false, attendanceRows = attendanceRows)
        }
    }
}