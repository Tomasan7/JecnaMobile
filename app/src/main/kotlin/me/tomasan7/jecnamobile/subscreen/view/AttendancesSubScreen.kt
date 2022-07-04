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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.AttendanceRow
import me.tomasan7.jecnamobile.subscreen.viewmodel.AttendancesSubScreenViewModel
import me.tomasan7.jecnamobile.util.getWeekDayName
import java.time.format.DateTimeFormatter

private val datePattern = DateTimeFormatter.ofPattern("d.M.")
private val timePattern = DateTimeFormatter.ofPattern("H:mm")

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
            uiState.attendanceRows.forEach { row ->
                AttendanceComposable(row)
            }
        }
    }
}

@Composable
private fun AttendanceComposable(
    attendanceRow: AttendanceRow,
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

            val dayName = getWeekDayName(attendanceRow.day.dayOfWeek)
            val dayDate = attendanceRow.day.format(datePattern)

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
                attendanceRow.attendancesList.forEach { attendance ->
                    Surface(
                        tonalElevation = 10.dp,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row (Modifier.padding(10.dp))
                        {
                            Text(attendance)
                        }
                    }
                }
            }
        }
    }
}