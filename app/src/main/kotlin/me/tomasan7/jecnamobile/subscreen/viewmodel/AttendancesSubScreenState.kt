package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.AttendancesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate
import java.time.Month

data class AttendancesSubScreenState(
    val loading: Boolean = true,
    val attendancesPage: AttendancesPage? = null,
    val selectedMonth: Month = LocalDate.now().month,
    val selectedSchoolYear: SchoolYear = SchoolYear(LocalDate.now())
)