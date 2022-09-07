package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.attendance.AttendancesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate
import java.time.Month

data class AttendancesState(
    val loading: Boolean = false,
    val attendancesPage: AttendancesPage? = null,
    val selectedMonth: Month = LocalDate.now().month,
    val selectedSchoolYear: SchoolYear = SchoolYear.current()
)