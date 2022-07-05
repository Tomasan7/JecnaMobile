package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.Attendance
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.mapToIntRange
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.AttendancesSubScreenViewModel
import me.tomasan7.jecnamobile.util.getMonthName
import me.tomasan7.jecnamobile.util.getWeekDayName
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

private val datePattern = DateTimeFormatter.ofPattern("d.M.")

@SubScreensNavGraph
@Destination
@Composable
fun AttendancesSubScreen(
    viewModel: AttendancesSubScreenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadAttendances() }
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                MonthSelector(uiState.selectedMonth) {
                    viewModel.selectMonth(it)
                }
                YearSelector(uiState.selectedSchoolYear) {
                    viewModel.selectSchoolYear(it)
                }
            }
            if (uiState.attendancesPage != null)
                uiState.attendancesPage.days.forEach { day ->
                    AttendanceComposable(day to uiState.attendancesPage[day])
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthSelector(selectedMonth: Month, onChange: (Month) -> Unit)
{
    var menuShown by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.width(160.dp),
        expanded = menuShown,
        onExpandedChange = { menuShown = !menuShown },
    ) {
        OutlinedTextField(
            shape = RoundedCornerShape(10.dp),
            readOnly = true,
            value = getMonthName(selectedMonth),
            onValueChange = {},
            label = { Text(stringResource(R.string.month)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuShown) }
        )
        ExposedDropdownMenu(
            expanded = menuShown,
            onDismissRequest = { menuShown = false },
        ) {
            Month.values().forEach { month ->
                DropdownMenuItem(
                    text = { Text(getMonthName(month)) },
                    onClick = {
                        menuShown = false
                        onChange(month)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSelector(selectedSchoolYear: SchoolYear, onChange: (SchoolYear) -> Unit)
{
    val currentSchoolYear = remember { SchoolYear(LocalDate.now()) }

    var menuShown by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.width(160.dp),
        expanded = menuShown,
        onExpandedChange = { menuShown = !menuShown },
    ) {
        OutlinedTextField(
            shape = RoundedCornerShape(10.dp),
            readOnly = true,
            value = selectedSchoolYear.toString(),
            onValueChange = {},
            label = { Text(stringResource(R.string.school_year)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuShown) }
        )

        val past4SchoolYears = remember { ((currentSchoolYear - 3)..currentSchoolYear).mapToIntRange { it.firstCalendarYear }.toList().map { SchoolYear(it) } }

        ExposedDropdownMenu(
            expanded = menuShown,
            onDismissRequest = { menuShown = false },
        ) {
            past4SchoolYears.forEach { schoolYear ->
                    DropdownMenuItem(
                        text = { Text(schoolYear.toString()) },
                        onClick = {
                            menuShown = false
                            onChange(schoolYear)
                        }
                    )
                }
        }
    }
}

@Composable
private fun AttendanceComposable(
    attendanceRow: Pair<LocalDate, List<Attendance>>,
    modifier: Modifier = Modifier
)
{
    Surface(
        modifier,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {

            val dayName = getWeekDayName(attendanceRow.first.dayOfWeek)
            val dayDate = attendanceRow.first.format(datePattern)

            Text(text = "$dayName $dayDate",
                 style = MaterialTheme.typography.titleMedium,
                 modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(15.dp))
            FlowRow(
                Modifier.fillMaxWidth(),
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
                mainAxisSpacing = 5.dp,
                crossAxisSpacing = 5.dp
            ) {
                attendanceRow.second.forEach { attendance ->
                    Surface(
                        tonalElevation = 10.dp,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(Modifier.padding(10.dp))
                        {
                            Text(attendance.toString())
                        }
                    }
                }
            }
        }
    }
}