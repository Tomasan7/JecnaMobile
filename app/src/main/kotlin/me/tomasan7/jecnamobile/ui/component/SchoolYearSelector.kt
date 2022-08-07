package me.tomasan7.jecnamobile.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.R
import java.time.LocalDate

@Composable
fun SchoolYearSelector(
    selectedSchoolYear: SchoolYear,
    modifier: Modifier = Modifier,
    showYearAhead: Boolean = false,
    onChange: (SchoolYear) -> Unit
)
{
    /* List of four past SchoolYears including the current one. (optionally the following one) */
    val past4SchoolYears = remember {
        val currentSchoolYear = SchoolYear(LocalDate.now())
        val rangeEnd = if (showYearAhead) currentSchoolYear + 1 else currentSchoolYear
        ((currentSchoolYear - 3)..rangeEnd).toList()
    }

    PeriodSelector(
        label = stringResource(R.string.school_year),
        options = past4SchoolYears,
        selectedValue = selectedSchoolYear,
        modifier = modifier,
        onChange = onChange
    )
}