package me.tomasan7.jecnamobile.grades

import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import javax.inject.Inject

class GradesRepositoryImpl @Inject constructor(
    private val jecnaClient: JecnaClient
) : GradesRepository
{
    override suspend fun getGradesPage() = jecnaClient.getGradesPage()

    override suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) = jecnaClient.getGradesPage(schoolYear, schoolYearHalf)
}
