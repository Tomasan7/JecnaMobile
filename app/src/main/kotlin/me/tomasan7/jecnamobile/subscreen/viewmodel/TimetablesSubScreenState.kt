package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear

data class TimetableState(
    val loading: Boolean = false,
    val timetablePage: TimetablePage? = null,
    val selectedSchoolYear: SchoolYear = SchoolYear.current(),
    val mostLessonsInLessonSpotInEachDay: Map<String, Int>? = timetablePage?.run {
        val result = mutableMapOf<String, Int>()

        for (day in days)
        {
            var dayResult = 0

            for (lessonSpot in getLessonsForDay(day))
                if (lessonSpot != null && lessonSpot.size > dayResult)
                    dayResult = lessonSpot.size

            result[day] = dayResult
        }

        result
    },
    val selectedPeriod: TimetablePage.PeriodOption? = run {
        timetablePage ?: return@run null
        timetablePage.periodOptions.find { it.selected }
    }
)