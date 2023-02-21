package me.tomasan7.jecnamobile.attendances

import me.tomasan7.jecnaapi.data.attendance.AttendancesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.Month

interface AttendancesRepository
{
    suspend fun getAttendancesPage(): AttendancesPage

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month): AttendancesPage
}