package me.tomasan7.jecnamobile

import me.tomasan7.jecnaapi.repository.WebAttendancesRepository
import me.tomasan7.jecnaapi.repository.WebGradesRepository
import me.tomasan7.jecnaapi.repository.WebTimetableRepository
import me.tomasan7.jecnaapi.web.JecnaWebClient

object RepositoryContainer
{
    lateinit var gradesRepository: WebGradesRepository
        private set
    lateinit var attendancesRepository: WebAttendancesRepository
        private set
    lateinit var timetableRepository: WebTimetableRepository
        private set

    fun init(jecnaWebClient: JecnaWebClient)
    {
        if (isInitialized())
            return

        gradesRepository = WebGradesRepository(jecnaWebClient)
        attendancesRepository = WebAttendancesRepository(jecnaWebClient)
        timetableRepository = WebTimetableRepository(jecnaWebClient)
    }

    fun isInitialized() = ::gradesRepository.isInitialized
}