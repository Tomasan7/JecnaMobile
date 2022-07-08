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
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.AttendancesSubScreenViewModel
import me.tomasan7.jecnamobile.ui.component.MonthSelector
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
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
            modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
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
                uiState.attendancesPage.days.forEach { day ->
                    AttendanceComposable(
                        modifier = Modifier.fillMaxWidth(),
                        attendanceRow = day to uiState.attendancesPage[day]
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
        modifier = modifier,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.fillMaxSize()
                       .padding(20.dp)
        ) {
            val dayName = getWeekDayName(attendanceRow.first.dayOfWeek)
            val dayDate = attendanceRow.first.format(datePattern)

            Text(
                text = "$dayName $dayDate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(15.dp))

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
                            text = attendance.toString(),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}