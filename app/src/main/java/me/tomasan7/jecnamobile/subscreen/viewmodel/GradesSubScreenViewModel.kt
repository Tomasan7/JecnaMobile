package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.repository.WebGradesRepository
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.LoginScreenViewModel
import java.util.LinkedList

class GradesSubScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val context
        get() = getApplication<Application>().applicationContext

    private val authPreferences
        get() = context.getSharedPreferences(LoginScreenViewModel.AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(GradesSubScreenState())
        private set

    private val gradesRepository: WebGradesRepository

    init
    {
        val client =
            JecnaWebClient(authPreferences.getString(LoginScreenViewModel.PREFERENCES_USERNAME_KEY, null) as String,
                           authPreferences.getString(
                               LoginScreenViewModel.PREFERENCES_PASSWORD_KEY, null) as String)

        gradesRepository = WebGradesRepository(client)

        viewModelScope.launch {
            client.login()
            val grades = gradesRepository.queryGrades()

            val subjects: MutableList<Subject> = LinkedList()

            for (subject in grades.subjects)
                subjects.add(Subject(subject, grades.getGradesForSubject(subject)))

            uiState = uiState.copy(subjects = subjects)
        }
    }
}