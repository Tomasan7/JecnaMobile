package me.tomasan7.jecnamobile.timetable

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import de.palm.composestateevents.EventEffect
import me.tomasan7.jecnaapi.data.timetable.*
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.component.*
import me.tomasan7.jecnamobile.util.getWeekDayName
import me.tomasan7.jecnamobile.util.manipulate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SubScreensNavGraph(start = true)
@Destination
@Composable
fun TimetableSubScreen(
    onHamburgerClick: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
)
{
    DisposableEffect(Unit) {
        viewModel.enteredComposition()
        onDispose {
            viewModel.leftComposition()
        }
    }

    val uiState = viewModel.uiState
    val dialogState = rememberObjectDialogState<Lesson>()
    val pullRefreshState = rememberPullRefreshState(uiState.loading, viewModel::reload)
    val snackbarHostState = remember { SnackbarHostState() }

    EventEffect(
        event = uiState.snackBarMessageEvent,
        onConsumed = viewModel::onSnackBarMessageEventConsumed
    ) {
        snackbarHostState.showSnackbar(it)
    }

    Scaffold(
        topBar = { SubScreenTopAppBar(R.string.sidebar_timetable, onHamburgerClick = onHamburgerClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PeriodSelectors(
                    modifier = Modifier.fillMaxWidth(),
                    timetablePeriodOptions = uiState.timetablePage?.periodOptions ?: emptyList(),
                    selectedSchoolYear = uiState.selectedSchoolYear,
                    selectedTimetablePeriod = uiState.timetablePage?.periodOptions?.find { it.selected },
                    onChangeSchoolYear = { viewModel.selectSchoolYear(it) },
                    onChangeTimetablePeriod = { viewModel.selectTimetablePeriod(it) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
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
                                DayLabel(
                                    getWeekDayName(day).substring(0, 2), Modifier
                                        .width(30.dp)
                                        .fillMaxHeight()
                                )
                                Spacer(Modifier.width(5.dp))
                                timetable.getLessonSpotsForDay(day)!!.forEach { lessonSpot ->
                                    LessonSpot(
                                        lessonSpot = lessonSpot,
                                        onLessonClick = { dialogState.show(it) },
                                        current = timetable.getCurrentLessonSpot() === lessonSpot,
                                        next = timetable.getCurrentNextLessonSpot(takeEmpty = true) === lessonSpot
                                    )
                                    Spacer(Modifier.width(5.dp))
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    ObjectDialog(
        state = dialogState,
        onDismissRequest = { dialogState.hide() },
        content = { LessonDialogContent(it) }
    )
}

@Composable
private fun PeriodSelectors(
    timetablePeriodOptions: List<TimetablePage.PeriodOption>,
    selectedSchoolYear: SchoolYear,
    selectedTimetablePeriod: TimetablePage.PeriodOption?,
    onChangeSchoolYear: (SchoolYear) -> Unit,
    onChangeTimetablePeriod: (TimetablePage.PeriodOption) -> Unit,
    modifier: Modifier = Modifier
)
{
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SchoolYearSelector(
            modifier = Modifier.width(160.dp),
            selectedSchoolYear = selectedSchoolYear,
            onChange = onChangeSchoolYear
        )
        TimetablePeriodSelector(
            modifier = Modifier.width(160.dp),
            periodOptions = timetablePeriodOptions,
            selectedOption = selectedTimetablePeriod,
            onChange = onChangeTimetablePeriod
        )
    }
}

@Composable
private fun TimetablePeriodSelector(
    periodOptions: List<TimetablePage.PeriodOption>,
    modifier: Modifier = Modifier,
    selectedOption: TimetablePage.PeriodOption?,
    onChange: (TimetablePage.PeriodOption) -> Unit
)
{
    OutlinedDropDownSelector(
        modifier = modifier,
        label = stringResource(R.string.timetable_period),
        options = periodOptions,
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
private fun LessonSpot(
    lessonSpot: LessonSpot,
    onLessonClick: (Lesson) -> Unit = {},
    current: Boolean = false,
    next: Boolean = false
)
{
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

            Lesson(
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
private fun Lesson(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
            if (lesson.teacherName?.short != null)
                Text(lesson.teacherName!!.short!!, Modifier.align(Alignment.TopStart))
            if (lesson.classroom != null)
                Text(lesson.classroom!!, Modifier.align(Alignment.TopEnd))
            if (lesson.group != null)
                Text(lesson.group!!, Modifier.align(Alignment.BottomEnd), fontSize = 10.sp)
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
private fun LessonDialogContent(lesson: Lesson)
{
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
            if (lesson.teacherName != null)
                DialogRow(stringResource(R.string.timetable_dialog_teacher), lesson.teacherName!!.full)
            DialogRow(stringResource(R.string.timetable_dialog_classroom), lesson.classroom ?: "")
            if (lesson.group != null)
                DialogRow(stringResource(R.string.timetable_dialog_group), lesson.group!!)
        }
    }
}