package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.icu.text.Collator
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import java.time.LocalDate
import java.time.Month
import java.util.*

data class GradesSubScreenState(
    val loading: Boolean = true,
    val gradesPage: GradesPage? = null,
    val selectedSchoolYear: SchoolYear = SchoolYear(LocalDate.now()),
    val selectedSchoolYearHalf: SchoolYearHalf = if (LocalDate.now().month !in Month.FEBRUARY..Month.AUGUST)
        SchoolYearHalf.FIRST
    else
        SchoolYearHalf.SECOND
)
{
    /**
     * [gradesPage]'s `subjectNames` sorted according to Czech alphabet. Is `null` when [gradesPage] is `null`.
     */
    val subjectNamesSorted = gradesPage?.subjectNames?.sortedWith(compareBy(Collator.getInstance(Locale("cs"))) { it.full })
}