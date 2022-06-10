package me.tomasan7.jecnamobile.subscreen.viewmodel

import java.time.LocalDate

data class AttendancesSubScreenState(val loading: Boolean = true, val attendanceRows: List<AttendanceRow> = emptyList())

data class AttendanceRow (val day: LocalDate, val attendancesList: List<String>)