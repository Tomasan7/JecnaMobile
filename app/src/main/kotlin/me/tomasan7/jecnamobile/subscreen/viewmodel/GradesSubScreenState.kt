package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import java.time.LocalDate
import java.time.Month

data class GradesSubScreenState(val loading: Boolean = true,
                                val gradesPage: GradesPage? = null,
                                val selectedSchoolYear: SchoolYear = SchoolYear(LocalDate.now()),
                                val selectedSchoolYearHalf: SchoolYearHalf = if (LocalDate.now().month !in Month.FEBRUARY..Month.AUGUST)
                                    SchoolYearHalf.FIRST else SchoolYearHalf.SECOND
)