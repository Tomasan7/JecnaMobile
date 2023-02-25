package me.tomasan7.jecnamobile.timetable

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
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.JecnaMobileApplication
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.util.createBroadcastReceiver
import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val repository: CacheTimetableRepository
) : ViewModel()
{
    var uiState by mutableStateOf(TimetableState())
        private set

    private var loadTimetableJob: Job? = null

    private val loginBroadcastReceiver = createBroadcastReceiver { _, _ ->
        if (loadTimetableJob == null || loadTimetableJob!!.isCompleted)
        {
            changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.back_online)))
            loadReal()
        }
    }

    init
    {
        loadCache()
        loadReal()
    }

    fun enteredComposition()
    {
        appContext.registerReceiver(
            loginBroadcastReceiver, IntentFilter(JecnaMobileApplication.SUCCESSFUL_LOGIN_ACTION)
        )
    }

    fun leftComposition()
    {
        loadTimetableJob?.cancel()
        appContext.unregisterReceiver(loginBroadcastReceiver)
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        changeUiState(selectedSchoolYear = schoolYear)
        loadReal()
    }

    fun selectTimetablePeriod(timetablePeriod: TimetablePage.PeriodOption)
    {
        changeUiState(selectedPeriod = timetablePeriod)
        loadReal()
    }

    private fun loadCache()
    {
        if (!repository.isCacheAvailable())
            return

        viewModelScope.launch {
            val cachedGrades = repository.getCachedTimetable() ?: return@launch

            changeUiState(
                timetablePage = cachedGrades.data,
                lastUpdateTimestamp = cachedGrades.timestamp
            )
        }
    }

    private fun loadReal()
    {
        loadTimetableJob?.cancel()

        changeUiState(loading = true)

        loadTimetableJob = viewModelScope.launch {
            try
            {
                val realTimetable = if (uiState.selectedPeriod == null)
                    repository.getRealTimetable()
                else
                    repository.getRealTimetable(uiState.selectedSchoolYear, uiState.selectedPeriod!!)

                changeUiState(
                    timetablePage = realTimetable,
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
            appContext.getString(R.string.showing_offline_data_time, timeStr)
        }
        else
        {
            val dateStr = localDateTime.format(OFFLINE_MESSAGE_DATE_FORMATTER)
            appContext.getString(R.string.showing_offline_data_date, dateStr)
        }
    }

    fun reload() = loadReal()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    private fun changeUiState(
        loading: Boolean = uiState.loading,
        timetablePage: TimetablePage? = uiState.timetablePage,
        lastUpdateTimestamp: Instant? = uiState.lastUpdateTimestamp,
        selectedSchoolYear: SchoolYear = uiState.selectedSchoolYear,
        selectedPeriod: TimetablePage.PeriodOption? = uiState.selectedPeriod,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent
    )
    {
        uiState = uiState.copy(
            loading = loading,
            timetablePage = timetablePage,
            lastUpdateTimestamp = lastUpdateTimestamp,
            selectedSchoolYear = selectedSchoolYear,
            selectedPeriod = selectedPeriod,
            snackBarMessageEvent = snackBarMessageEvent
        )
    }

    companion object
    {
        val OFFLINE_MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        val OFFLINE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d. M.")
    }
}