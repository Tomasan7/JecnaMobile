package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate

data class TimetableSubScreenState(
    val loading: Boolean = true,
    val timetablePage: TimetablePage? = null,
    val selectedSchoolYear: SchoolYear = SchoolYear.current(),
    val selectedPeriod: TimetablePage.PeriodOption? = run {
        timetablePage ?: return@run null
        timetablePage.periodOptions.find { it.selected }
    }
)