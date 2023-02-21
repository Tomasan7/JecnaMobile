package me.tomasan7.jecnamobile.attendances

import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.Month
import javax.inject.Inject

class AttendancesRepositoryImpl @Inject constructor(
    val jecnaClient: JecnaClient
) : AttendancesRepository
{
    override suspend fun getAttendancesPage() = jecnaClient.getAttendancesPage()

    override suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) = jecnaClient.getAttendancesPage(schoolYear, month)
}