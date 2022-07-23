package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.Lesson
import me.tomasan7.jecnaapi.data.LessonPeriod
import me.tomasan7.jecnaapi.data.LessonSpot
import me.tomasan7.jecnaapi.data.TimetablePage
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.TimetableSubScreenViewModel
import me.tomasan7.jecnamobile.ui.component.PeriodSelector
import me.tomasan7.jecnamobile.ui.component.PeriodSelectorNullable
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
import me.tomasan7.jecnamobile.util.manipulate
import me.tomasan7.jecnamobile.util.toRoman
import java.time.DayOfWeek
import java.time.LocalDateTime

@SubScreensNavGraph
@Destination
@Composable
fun TimetableSubScreen(
    viewModel: TimetableSubScreenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadTimetable() }
    ) {
        Column(
            modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SchoolYearSelector(
                    modifier = Modifier.width(160.dp),
                    selectedSchoolYear = uiState.selectedSchoolYear,
                    onChange = { viewModel.selectSchoolYear(it) }
                )

                TimetablePeriodSelector(
                    modifier = Modifier.width(160.dp),
                    periodOptions = uiState.timetablePage?.periodOptions ?: emptyList(),
                    selectedOption = uiState.selectedPeriod,
                    onChange = { viewModel.selectPeriod(it) }
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {


                if (uiState.timetablePage != null)
                {
                    Row {
                        uiState.timetablePage.lessonPeriods.forEachIndexed { i, lessonPeriod ->
                            if (i == 0)
                                Spacer(Modifier.width(46.dp))

                            Spacer(Modifier.width(5.dp))

                            TimetableLessonPeriod(
                                modifier = Modifier.size(width = 100.dp, height = 50.dp),
                                lessonPeriod = lessonPeriod,
                                hourIndex = i + 1
                            )
                        }
                    }

                    uiState.timetablePage.daysSorted.forEach { day ->
                        Row {
                            Spacer(Modifier.width(16.dp))
                            DayLabel(day)
                            Spacer(Modifier.width(5.dp))
                            uiState.timetablePage.getLessonsForDay(day).forEachIndexed { i, lessonSpot ->
                                val lessonPeriod = uiState.timetablePage.lessonPeriods[i]
                                val now = LocalDateTime.now()

                                val current = now.toLocalTime() in lessonPeriod.from..lessonPeriod.to
                                              && timetableDayLabelToDayOfWeek(day) == now.dayOfWeek

                                TimetableLessonSpot(lessonSpot, current)
                                Spacer(Modifier.width(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetablePeriodSelector(
    periodOptions: List<TimetablePage.PeriodOption>?,
    selectedOption: TimetablePage.PeriodOption? = if (periodOptions != null && periodOptions.isNotEmpty()) periodOptions[0] else null,
    modifier: Modifier = Modifier,
    onChange: (TimetablePage.PeriodOption) -> Unit
)
{
    PeriodSelectorNullable(
        modifier = modifier,
        label = stringResource(R.string.timetable_period),
        options = periodOptions ?: emptyList(),
        optionStringMap = { it?.value ?: "" },
        selectedValue = selectedOption,
        onChange = onChange
    )
}

@Composable
private fun TimetableLessonPeriod(
    lessonPeriod: LessonPeriod,
    hourIndex: Int,
    modifier: Modifier = Modifier
)
{
    Surface(
        modifier = modifier,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).manipulate(1.5f),
        shape = RoundedCornerShape(5.dp),
    ) {
        Box(Modifier.padding(4.dp)) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = hourIndex.toString(),
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = lessonPeriod.toString(),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TimetableLessonSpot(
    lessonSpot: LessonSpot?,
    current: Boolean = false
)
{
    Column(Modifier.size(100.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lessonSpot?.forEach { lesson ->
            TimetableLesson(
                modifier = Modifier.fillMaxWidth().weight(1F),
                lesson = lesson,
                current = current
            )
        }
    }
}

@Composable
private fun TimetableLesson(
    modifier: Modifier = Modifier,
    lesson: Lesson,
    current: Boolean = false
)
{
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(5.dp),
        color = if (current) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surface
    ) {
        Box(Modifier.padding(4.dp)) {
            if (lesson.subjectName.short != null)
                Text(lesson.subjectName.short!!, Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold)
            if (lesson.teacherName.short != null)
                Text(lesson.teacherName.short!!, Modifier.align(Alignment.TopStart))
            Text(lesson.classroom, Modifier.align(Alignment.TopEnd))
            if (lesson.group != 0)
                Text(lesson.group.toRoman(), Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun DayLabel(day: String)
{
    Surface(
        modifier = Modifier.size(width = 30.dp, height = 100.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).manipulate(1.5f),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(5.dp)
    ) {
        Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
            Text(text = day, fontWeight = FontWeight.Bold)
        }
    }
}

fun timetableDayLabelToDayOfWeek(timetableDayLabel: String) = when (timetableDayLabel)
{
    "Po" -> DayOfWeek.MONDAY
    "Út" -> DayOfWeek.TUESDAY
    "St" -> DayOfWeek.WEDNESDAY
    "Čt" -> DayOfWeek.THURSDAY
    "Pa" -> DayOfWeek.FRIDAY
    else -> throw RuntimeException("'$timetableDayLabel' doesn't match any day of week.")
}