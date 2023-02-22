package me.tomasan7.jecnamobile.teachers.teacher

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.schoolStaff.Teacher
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.web.jecna.JecnaWebClient
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.teachers.TeachersRepository
import okhttp3.Headers
import javax.inject.Inject

@HiltViewModel
class TeacherViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val jecnaClient: JecnaClient,
    private val repository: TeachersRepository
) : ViewModel()
{
    private lateinit var teacherReference: TeacherReference

    var uiState by mutableStateOf(TeacherState())
        private set

    private var loadTeacherJob: Job? = null

    fun enteredComposition(teacherReference: TeacherReference)
    {
        this.teacherReference = teacherReference
        loadReal()
    }

    fun leftComposition()
    {
        loadTeacherJob?.cancel()
    }

    fun createImageRequest(path: String) = ImageRequest.Builder(appContext).apply {
        data(JecnaWebClient.getUrlForPath(path))
        crossfade(true)
        val sessionCookie = getSessionCookieBlocking() ?: return@apply
        headers(Headers.headersOf("Cookie", sessionCookie.toHeaderString()))
    }.build()

    private fun getSessionCookieBlocking() = runBlocking { jecnaClient.getSessionCookie() }

    private fun Cookie.toHeaderString() = "$name=$value"

    private fun loadReal()
    {
        loadTeacherJob?.cancel()

        changeUiState(loading = true)

        loadTeacherJob = viewModelScope.launch {
            try
            {
                changeUiState(teacher = repository.getTeacher(teacherReference))
            }
            catch (e: UnresolvedAddressException)
            {
                changeUiState(snackBarMessageEvent = triggered(getOfflineMessage()))
            }
            finally
            {
                changeUiState(loading = false)
            }
        }
    }

    private fun getOfflineMessage() = appContext.getString(R.string.no_internet_connection)

    fun reload() = loadReal()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    fun changeUiState(
        loading: Boolean = uiState.loading,
        teacher: Teacher? = uiState.teacher,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent
    )
    {
        uiState = TeacherState(
            loading = loading,
            teacher = teacher,
            snackBarMessageEvent = snackBarMessageEvent
        )
    }
}