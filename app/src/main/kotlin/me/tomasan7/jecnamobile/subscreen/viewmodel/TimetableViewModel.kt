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
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.repository.TimetableRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.R
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    @ApplicationContext
    private val appContext: Context
) : ViewModel()
{
    var uiState by mutableStateOf(TimetableState())
        private set

    private var timetableLoadJob: Job? = null

    init
    {
        loadTimetable()
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        uiState = uiState.copy(selectedSchoolYear = schoolYear)
        loadTimetable()
    }

    fun selectPeriod(periodOption: TimetablePage.PeriodOption)
    {
        uiState = uiState.copy(selectedPeriod = periodOption)
        loadTimetable()
    }

    fun loadTimetable()
    {
        uiState = uiState.copy(loading = true)

        timetableLoadJob?.cancel()

        timetableLoadJob = viewModelScope.launch {
            try
            {
                val timetablePage = if (uiState.selectedPeriod == null)
                    timetableRepository.queryTimetablePage()
                else
                    timetableRepository.queryTimetablePage(uiState.selectedSchoolYear, uiState.selectedPeriod!!)

                uiState = uiState.copy(
                    loading = false,
                    timetablePage = timetablePage,
                    selectedPeriod = timetablePage.periodOptions.find { it.selected },
                    mostLessonsInLessonSpotInEachDay = TimetableState(timetablePage = timetablePage).mostLessonsInLessonSpotInEachDay
                )
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.unsupported_timetable), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_load), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
        }
    }
}