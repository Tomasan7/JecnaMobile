package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.timetable.Lesson
import me.tomasan7.jecnaapi.data.timetable.LessonPeriod
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.data.timetable.TimetableSpot
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.TimetableViewModel
import me.tomasan7.jecnamobile.ui.component.DialogRow
import me.tomasan7.jecnamobile.ui.component.PeriodSelectorNullable
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
import me.tomasan7.jecnamobile.util.getWeekDayName
import me.tomasan7.jecnamobile.util.manipulate
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import me.tomasan7.jecnamobile.util.toRoman

@SubScreensNavGraph
@Destination
@Composable
fun TimetableSubScreen(
    viewModel: TimetableViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState
    var dialogOpened by rememberMutableStateOf(false)
    var dialogLesson by rememberMutableStateOf<Lesson?>(null)

    fun showDialog(lesson: Lesson)
    {
        dialogOpened = true
        dialogLesson = lesson
    }

    fun hideDialog()
    {
        dialogOpened = false
        dialogLesson = null
    }

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
                    showYearAhead = true,
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
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {


                if (uiState.timetablePage != null)
                {
                    val timetable = uiState.timetablePage.timetable

                    Row {
                        timetable.lessonPeriods.forEachIndexed { i, lessonPeriod ->
                            if (i == 0)
                                Spacer(Modifier.width(30.dp))

                            Spacer(Modifier.width(5.dp))

                            TimetableLessonPeriod(
                                modifier = Modifier.size(width = 100.dp, height = 50.dp),
                                lessonPeriod = lessonPeriod,
                                hourIndex = i + 1
                            )
                        }
                    }

                    timetable.daysSorted.forEach { day ->
                        val modifier = if (uiState.mostLessonsInLessonSpotInEachDay!![day]!! <= 2)
                            Modifier.height(100.dp)
                        else
                            Modifier.height(IntrinsicSize.Min)
                        Row(modifier) {
                            DayLabel(getWeekDayName(day).substring(0, 2), Modifier.width(30.dp).fillMaxHeight())
                            Spacer(Modifier.width(5.dp))
                            timetable.getTimetableSpotsForDay(day)!!.forEach { timetableSpot ->
                                TimetableSpot(
                                    timetableSpot = timetableSpot,
                                    onLessonClick = { showDialog(it) },
                                    current = timetable.getCurrentTimetableSpot() == timetableSpot,
                                    next = timetable.getCurrentNextTimetableSpot() == timetableSpot
                                )
                                Spacer(Modifier.width(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    /* Show the grade dialog. */
    if (dialogOpened && dialogLesson != null)
        LessonDialog(dialogLesson!!, ::hideDialog)
}

@Composable
private fun TimetablePeriodSelector(
    periodOptions: List<TimetablePage.PeriodOption>?,
    selectedOption: TimetablePage.PeriodOption? = if (!periodOptions.isNullOrEmpty()) periodOptions[0] else null,
    modifier: Modifier = Modifier,
    onChange: (TimetablePage.PeriodOption) -> Unit
)
{
    PeriodSelectorNullable(
        modifier = modifier,
        label = stringResource(R.string.timetable_period),
        options = periodOptions ?: emptyList(),
        optionStringMap = { it?.toString() ?: "" },
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
private fun TimetableSpot(
    timetableSpot: TimetableSpot,
    onLessonClick: (Lesson) -> Unit = {},
    current: Boolean = false,
    next: Boolean = false
)
{
    val lessonSpot = timetableSpot.lessonSpot

    var lessonSpotModifier = Modifier.width(100.dp)

    if (lessonSpot.size <= 2)
        lessonSpotModifier = lessonSpotModifier.fillMaxHeight()

    Column(lessonSpotModifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lessonSpot.forEach { lesson ->
            /* If there is < 2 lessons, they are stretched to  */
            var lessonModifier = Modifier.fillMaxWidth()
            lessonModifier = if (lessonSpot.size <= 2)
                lessonModifier.weight(1f)
            else
                lessonModifier.height(50.dp)

            TimetableLesson(
                modifier = lessonModifier,
                onClick = { onLessonClick(lesson) },
                lesson = lesson,
                current = current,
                next = next
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableLesson(
    lesson: Lesson,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    current: Boolean = false,
    next: Boolean = false
)
{
    val shape = RoundedCornerShape(5.dp)
    Surface(
        modifier = if (next) modifier.border(1.dp, MaterialTheme.colorScheme.inverseSurface, shape) else modifier,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        shape = shape,
        color = if (current) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surface,
        onClick = onClick
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
private fun DayLabel(
    day: String,
    modifier: Modifier = Modifier
)
{
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).manipulate(1.5f),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(5.dp)
    ) {
        Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
            Text(text = day, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LessonDialog(lesson: Lesson, onDismiss: () -> Unit)
{
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 5.dp,
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DialogRow(stringResource(R.string.timetable_dialog_subject), lesson.subjectName.full)
                DialogRow(stringResource(R.string.timetable_dialog_teacher), lesson.teacherName.full)
                DialogRow(stringResource(R.string.timetable_dialog_classroom), lesson.classroom)
                if (lesson.group != 0)
                    DialogRow(stringResource(R.string.timetable_dialog_group), lesson.group.toRoman())
            }
        }
    }
}