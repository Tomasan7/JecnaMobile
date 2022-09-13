package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.attendance.Attendance
import me.tomasan7.jecnaapi.data.attendance.AttendanceType
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.AttendancesViewModel
import me.tomasan7.jecnamobile.ui.component.Card
import me.tomasan7.jecnamobile.ui.component.MonthSelector
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
import me.tomasan7.jecnamobile.util.getWeekDayName
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@SubScreensNavGraph
@Destination
@Composable
fun AttendancesSubScreen(
    viewModel: AttendancesViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadAttendances() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SchoolYearSelector(
                    modifier = Modifier.width(160.dp),
                    selectedSchoolYear = uiState.selectedSchoolYear,
                    onChange = { viewModel.selectSchoolYear(it) }
                )

                MonthSelector(
                    modifier = Modifier.width(160.dp),
                    selectedMonth = uiState.selectedMonth,
                    onChange = { viewModel.selectMonth(it) }
                )
            }

            if (uiState.attendancesPage != null)
                uiState.daysSorted!!.forEach { day ->
                    val attendance = uiState.attendancesPage[day]
                    AttendanceComposable(day to attendance)
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
    val dayName = getWeekDayName(attendanceRow.first.dayOfWeek)
    val dayDate = attendanceRow.first.format(DATE_FORMATTER)

    Card(
        title = {
            Text(
                text = "$dayName $dayDate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier
    ) {
        FlowRow(
            Modifier.fillMaxWidth(),
            crossAxisAlignment = FlowCrossAxisAlignment.Center,
            mainAxisSpacing = 7.dp,
            crossAxisSpacing = 7.dp
        ) {
            attendanceRow.second.forEach { attendance ->
                    Surface(
                        tonalElevation = 10.dp,
                        shadowElevation = 2.dp,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${getAttendanceTypeName(attendance.type)} ${attendance.time.format(TIME_FORMATTER)}",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
    }
}

@Composable
private fun getAttendanceTypeName(type: AttendanceType) = when(type)
{
    AttendanceType.ENTER -> stringResource(R.string.attendance_type_enter)
    AttendanceType.EXIT -> stringResource(R.string.attendance_type_exit)
}

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.")

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")