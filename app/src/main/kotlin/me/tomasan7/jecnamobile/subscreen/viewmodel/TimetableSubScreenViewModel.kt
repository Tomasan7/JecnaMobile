package me.tomasan7.jecnamobile.subscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.data.TimetablePage
import me.tomasan7.jecnaapi.repository.TimetableRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import javax.inject.Inject

@HiltViewModel
class TimetableSubScreenViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository
) : ViewModel()
{
    var uiState by mutableStateOf(TimetableSubScreenState())
        private set

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

        viewModelScope.launch {
            val timetablePage = if (uiState.selectedPeriod == null)
                timetableRepository.queryTimetablePage()
            else
                timetableRepository.queryTimetablePage(uiState.selectedSchoolYear, uiState.selectedPeriod!!)

            uiState = uiState.copy(
                loading = false,
                timetablePage = timetablePage,
                selectedPeriod = timetablePage.periodOptions.find { it.selected },
                mostLessonsInLessonSpotInEachDay = TimetableSubScreenState(timetablePage = timetablePage).mostLessonsInLessonSpotInEachDay
            )
        }
    }
}