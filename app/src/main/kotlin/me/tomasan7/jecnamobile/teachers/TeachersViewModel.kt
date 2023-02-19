package me.tomasan7.jecnamobile.teachers

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import io.ktor.util.network.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.data.schoolStaff.TeachersPage
import me.tomasan7.jecnamobile.R
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TeachersViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val repository: TeachersRepository
) : ViewModel()
{
    var uiState by mutableStateOf(TeachersState())
        private set

    private var loadTeachersJob: Job? = null

    fun enteredComposition()
    {
        loadReal()
    }

    fun leftComposition()
    {
        loadTeachersJob?.cancel()
    }

    fun onFilterFieldValueChange(value: String) = changeUiState(filterFieldValue = value)

    private fun loadReal()
    {
        loadTeachersJob?.cancel()

        changeUiState(loading = true)

        loadTeachersJob = viewModelScope.launch {
            try
            {
                changeUiState(teachersPage = repository.getTeachersPage(),)
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

    private fun changeUiState(
        loading: Boolean = uiState.loading,
        teachersPage: TeachersPage? = uiState.teachersPage,
        filterFieldValue: String = uiState.filterFieldValue,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            teachersPage = teachersPage,
            filterFieldValue = filterFieldValue,
            snackBarMessageEvent = snackBarMessageEvent
        )
    }

    companion object
    {
        val OFFLINE_MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        val OFFLINE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d. M.")
    }
}