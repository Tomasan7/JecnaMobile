package me.tomasan7.jecnamobile.timetable

import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.DayOfWeek
import java.time.Instant
import java.util.*

data class TimetableState(
    val loading: Boolean = false,
    val timetablePage: TimetablePage? = null,
    val lastUpdateTimestamp: Instant? = null,
    val selectedSchoolYear: SchoolYear = timetablePage?.selectedSchoolYear ?: SchoolYear.current(),
    val selectedPeriod: TimetablePage.PeriodOption? = timetablePage?.periodOptions?.find { it.selected },
    val snackBarMessageEvent: StateEventWithContent<String> = consumed()
)
{
    val mostLessonsInLessonSpotInEachDay: Map<DayOfWeek, Int>? = timetablePage?.timetable?.run {
        val result = mutableMapOf<DayOfWeek, Int>()

        for (day in days)
        {
            var dayResult = 0

            for (lessonSpot in getLessonSpotsForDay(day)!!)
                if (lessonSpot.size > dayResult)
                    dayResult = lessonSpot.size

            result[day] = dayResult
        }

        result
    }
}