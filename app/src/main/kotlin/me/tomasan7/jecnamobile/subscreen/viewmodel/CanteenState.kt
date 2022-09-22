package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.canteen.Menu
import java.time.LocalDate
import java.time.LocalTime

data class CanteenState(
    val loading: Boolean = false,
    val orderInProcess: Boolean = false,
    val menu: Menu? = null
)
{
    val futureDayMenusSorted = menu?.dayMenus?.filter {
        val nowDate = LocalDate.now()

        val isBeforeToday = it.day.isBefore(nowDate)
        val isToday = it.day == nowDate
        val isTomorrow = it.day == nowDate.withDayOfMonth(nowDate.dayOfMonth + 1)
        val isItAfterOrderTime = LocalTime.now().isAfter(ORDER_END_TIME)

        !(isBeforeToday || isToday || isTomorrow && isItAfterOrderTime)
    }?.sortedBy { it.day }

    companion object
    {
        /**
         * The time after which you can no longer order food for tomorrow.
         */
        private val ORDER_END_TIME = LocalTime.of(14, 0)
    }
}