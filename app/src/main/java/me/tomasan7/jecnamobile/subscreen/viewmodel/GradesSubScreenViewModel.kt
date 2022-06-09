package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.repository.WebGradesRepository
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.screen.viewmodel.LoginScreenViewModel
import java.util.LinkedList

class GradesSubScreenViewModel(application: Application, jecnaWebClient: JecnaWebClient?) : AndroidViewModel(application)
{
    private val appContext
        get() = getApplication<Application>().applicationContext

    private val authPreferences
        get() = appContext.getSharedPreferences(LoginScreenViewModel.AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(GradesSubScreenState())
        private set

    private lateinit var gradesRepository: WebGradesRepository

    init
    {
        if (jecnaWebClient == null)
        {
            viewModelScope.launch {
                val client = JecnaWebClient(authPreferences.getString(LoginScreenViewModel.PREFERENCES_USERNAME_KEY, null) as String,
                                            authPreferences.getString(
                                                LoginScreenViewModel.PREFERENCES_PASSWORD_KEY, null) as String)
                client.login()
                gradesRepository = WebGradesRepository(client)
                loadGrades()
            }
        }
        else
        {
            gradesRepository = WebGradesRepository(jecnaWebClient)
            loadGrades()
        }
    }

    fun loadGrades()
    {
        uiState = uiState.copy(loading = true)

        viewModelScope.launch {
            val grades = gradesRepository.queryGrades()

            val subjects: MutableList<Subject> = LinkedList()

            for (subject in grades.subjects)
                subjects.add(Subject(subject, grades.getGradesForSubject(subject)))

            uiState = uiState.copy(loading = false, subjects = subjects)
        }
    }
}