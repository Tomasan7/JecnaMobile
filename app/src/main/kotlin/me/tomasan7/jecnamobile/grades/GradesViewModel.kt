package me.tomasan7.jecnamobile.grades

import android.content.Context
import android.content.IntentFilter
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
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnamobile.JecnaMobileApplication
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.util.createBroadcastReceiver
import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val repository: CacheGradesRepository
) : ViewModel()
{
    var uiState by mutableStateOf(GradesState())
        private set

    private var loadGradesJob: Job? = null

    private val loginBroadcastReceiver = createBroadcastReceiver { _, _ ->
        if (loadGradesJob == null || loadGradesJob!!.isCompleted)
        {
            changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.back_online)))
            loadReal()
        }
    }

    fun enteredComposition()
    {
        loadCache()
        loadReal()
        appContext.registerReceiver(
            loginBroadcastReceiver, IntentFilter(JecnaMobileApplication.SUCCESSFUL_LOGIN_ACTION)
        )
    }

    fun leftComposition()
    {
        loadGradesJob?.cancel()
        appContext.unregisterReceiver(loginBroadcastReceiver)
    }

    fun selectSchoolYearHalf(schoolYearHalf: SchoolYearHalf)
    {
        changeUiState(selectedSchoolYearHalf = schoolYearHalf)
        loadReal()
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        changeUiState(selectedSchoolYear = schoolYear)
        loadReal()
    }

    private fun loadCache()
    {
        if (!repository.isCacheAvailable())
            return

        viewModelScope.launch {
            val cachedGrades = repository.getCachedGrades() ?: return@launch

            changeUiState(
                gradesPage = cachedGrades.data,
                lastUpdateTimestamp = cachedGrades.timestamp
            )
        }
    }

    private fun loadReal()
    {
        loadGradesJob?.cancel()

        changeUiState(loading = true)

        loadGradesJob = viewModelScope.launch {
            try
            {
                val realGrades = if (isSelectedPeriodCurrent())
                    repository.getRealGrades()
                else
                    repository.getRealGrades(uiState.selectedSchoolYear, uiState.selectedSchoolYearHalf)

                changeUiState(
                    gradesPage = realGrades,
                    lastUpdateTimestamp = Instant.now()
                )
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

    private fun getOfflineMessage(): String
    {
        val cacheTimestamp = uiState.lastUpdateTimestamp
        val localDateTime = LocalDateTime.ofInstant(cacheTimestamp, ZoneId.systemDefault())
        val localDate = localDateTime.toLocalDate()

        return if (localDate == LocalDate.now())
        {
            val time = localDateTime.toLocalTime()
            val timeStr = time.format(OFFLINE_MESSAGE_TIME_FORMATTER)
            appContext.getString(R.string.grade_showing_offline_data_time, timeStr)
        }
        else
        {
            val dateStr = localDateTime.format(OFFLINE_MESSAGE_DATE_FORMATTER)
            appContext.getString(R.string.grade_showing_offline_data_date, dateStr)
        }
    }

    fun reload() = loadReal()

    private fun isSelectedPeriodCurrent() =
        uiState.selectedSchoolYear == SchoolYear.current() && uiState.selectedSchoolYearHalf == SchoolYearHalf.current()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    private fun changeUiState(
        loading: Boolean = uiState.loading,
        gradesPage: GradesPage? = uiState.gradesPage,
        lastUpdateTimestamp: Instant? = uiState.lastUpdateTimestamp,
        selectedSchoolYear: SchoolYear = uiState.selectedSchoolYear,
        selectedSchoolYearHalf: SchoolYearHalf = uiState.selectedSchoolYearHalf,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            gradesPage = gradesPage,
            lastUpdateTimestamp = lastUpdateTimestamp,
            selectedSchoolYear = selectedSchoolYear,
            selectedSchoolYearHalf = selectedSchoolYearHalf,
            snackBarMessageEvent = snackBarMessageEvent
        )
    }

    companion object
    {
        val OFFLINE_MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        val OFFLINE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d. M.")
    }
}