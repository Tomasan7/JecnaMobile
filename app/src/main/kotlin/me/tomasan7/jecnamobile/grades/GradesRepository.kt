package me.tomasan7.jecnamobile.grades

import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf

interface GradesRepository
{
    suspend fun getGradesPage(): GradesPage
    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf): GradesPage
}
