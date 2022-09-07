package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.DayOfWeek

data class TimetableState(
    val loading: Boolean = false,
    val timetablePage: TimetablePage? = null,
    val selectedSchoolYear: SchoolYear = SchoolYear.current(),
    val mostLessonsInLessonSpotInEachDay: Map<DayOfWeek, Int>? = timetablePage?.timetable?.run {
        val result = mutableMapOf<DayOfWeek, Int>()

        for (day in days)
        {
            var dayResult = 0

            for (timetableSpot in getTimetableSpotsForDay(day)!!)
                if (timetableSpot.lessonSpot.size > dayResult)
                    dayResult = timetableSpot.lessonSpot.size

            result[day] = dayResult
        }

        result
    },
    val selectedPeriod: TimetablePage.PeriodOption? = run {
        timetablePage ?: return@run null
        timetablePage.periodOptions.find { it.selected }
    }
)