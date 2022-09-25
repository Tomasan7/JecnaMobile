package me.tomasan7.jecnamobile.subscreen.state

import me.tomasan7.jecnaapi.data.canteen.MenuPage
import java.time.LocalDate
import java.time.LocalTime

data class CanteenState(
    val loading: Boolean = false,
    val orderInProcess: Boolean = false,
    val menuPage: MenuPage? = null
)
{
    val futureDayMenusSorted = menuPage?.menu?.dayMenus?.filter {
        val nowDate = LocalDate.now()

        val isAfterToday = it.day.isAfter(nowDate)
        val isToday = it.day == nowDate
        val isItAfterHandOutEndTime = LocalTime.now().isAfter(FOOD_HAND_OUT_END_TIME)

        isAfterToday || isToday && !isItAfterHandOutEndTime
    }?.sortedBy { it.day }

    companion object
    {
        /**
         * The time after which you canteen no longer hands out food.
         */
        private val FOOD_HAND_OUT_END_TIME = LocalTime.of(14, 30)
    }
}