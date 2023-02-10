package me.tomasan7.jecnamobile.timetable

import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val jecnaClient: JecnaClient
) : TimetableRepository
{
    override suspend fun getTimetablePage() = jecnaClient.getTimetablePage()

    override suspend fun getTimetablePage(
        schoolYear: SchoolYear,
        timetablePeriod: TimetablePage.PeriodOption
    ) = jecnaClient.getTimetablePage(schoolYear, timetablePeriod)
}