package me.tomasan7.jecnamobile.timetable

import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf

interface TimetableRepository
{
    suspend fun getTimetablePage(): TimetablePage
    suspend fun getTimetablePage(schoolYear: SchoolYear, timetablePeriod: TimetablePage.PeriodOption): TimetablePage
}