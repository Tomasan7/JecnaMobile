package me.tomasan7.jecnamobile.timetable

import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear

interface TimetableRepository
{
    suspend fun getTimetablePage(): TimetablePage
    suspend fun getTimetablePage(schoolYear: SchoolYear, timetablePeriod: TimetablePage.PeriodOption): TimetablePage
}