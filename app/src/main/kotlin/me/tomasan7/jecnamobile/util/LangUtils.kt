package me.tomasan7.jecnamobile.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.tomasan7.jecnamobile.R
import java.time.DayOfWeek

@Composable
fun getWeekDayName(dayOfWeek: DayOfWeek) = when (dayOfWeek)
{
    DayOfWeek.MONDAY    -> stringResource(R.string.monday)
    DayOfWeek.TUESDAY   -> stringResource(R.string.tuesday)
    DayOfWeek.WEDNESDAY -> stringResource(R.string.wednesday)
    DayOfWeek.THURSDAY  -> stringResource(R.string.thursday)
    DayOfWeek.FRIDAY    -> stringResource(R.string.friday)
    DayOfWeek.SATURDAY  -> stringResource(R.string.saturday)
    DayOfWeek.SUNDAY    -> stringResource(R.string.sunday)
}