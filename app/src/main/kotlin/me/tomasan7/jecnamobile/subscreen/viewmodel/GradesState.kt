package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.icu.text.Collator
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import java.util.*

data class GradesState(
    val loading: Boolean = false,
    val gradesPage: GradesPage? = null,
    val selectedSchoolYear: SchoolYear = SchoolYear.current(),
    val selectedSchoolYearHalf: SchoolYearHalf = SchoolYearHalf.current()
)
{
    /**
     * [gradesPage]'s `subjectNames` sorted according to Czech alphabet. Is `null` when [gradesPage] is `null`.
     */
    val subjectNamesSorted = gradesPage?.subjectNames?.sortedWith(compareBy(Collator.getInstance(Locale("cs"))) { it.full })
}