package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.grade.*
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.GradesSubScreenViewModel
import me.tomasan7.jecnamobile.ui.component.SchoolYearHalfSelector
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
import me.tomasan7.jecnamobile.ui.component.VerticalDivider
import me.tomasan7.jecnamobile.util.getGradeColor
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@SubScreensNavGraph(start = true)
@Destination
@Composable
fun GradesSubScreen(
    viewModel: GradesSubScreenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState
    var dialogOpened by rememberMutableStateOf(false)
    var dialogGrade by rememberMutableStateOf<Grade?>(null)

    fun showDialog(grade: Grade)
    {
        dialogOpened = true
        dialogGrade = grade
    }

    fun hideDialog()
    {
        dialogOpened = false
        dialogGrade = null
    }

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadGrades() }
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
                SchoolYearHalfSelector(
                    modifier = Modifier.width(160.dp),
                    selectedSchoolYearHalf = uiState.selectedSchoolYearHalf,
                    onChange = { viewModel.selectSchoolYearHalf(it) }
                )
            }

            if (uiState.gradesPage != null)
            {
                /* subjectNamesSorted is not null, because it is only null when gradesPage is null. */
                uiState.subjectNamesSorted!!.forEach { subjectName ->
                    val subject = uiState.gradesPage[subjectName]!!
                    /* Using subject as a key, so once there's a different subject (ex. user changes period) the composables are recreated. (not just recomposed) */
                    key(subject) {
                        Subject(
                            subject = subject,
                            onGradeClick = { showDialog(it) }
                        )
                    }
                }

                Behaviour(uiState.gradesPage.behaviour)
            }

            /* Show the grade dialog. */
            if (dialogOpened && dialogGrade != null)
                GradeDialog(dialogGrade!!, ::hideDialog)
        }
    }
}

@Composable
private fun Container(
    title: String,
    rightColumnVisible: Boolean = true,
    rightColumnContent: @Composable ColumnScope.() -> Unit = {},
    modifier: Modifier,
    content: @Composable () -> Unit = {}
)
{
    var rowHeightValue by remember { mutableStateOf(0) }

    /* This is used as a workaround for not working Intrinsic Measurements in Flow layouts. */
    /* https://github.com/google/accompanist/issues/1236 */
    @Composable
    fun Modifier.rowHeight() = height(with(LocalDensity.current) { rowHeightValue.toDp() })

    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(Modifier.padding(20.dp)
                    .onSizeChanged { rowHeightValue = it.height }
        ) {
            Column(Modifier.weight(1f, true)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(15.dp))

                content()
            }

            if (!rightColumnVisible)
                return@Row

            VerticalDivider(
                modifier = Modifier.rowHeight().padding(horizontal = 10.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
            )

            Column(
                modifier = Modifier.rowHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                content = rightColumnContent
            )
        }
    }
}

@Composable
private fun Subject(
    subject: Subject,
    onGradeClick: (Grade) -> Unit = {}
)
{
    Container(
        modifier = Modifier.fillMaxWidth(),
        title = subject.name.full,
        rightColumnVisible = !subject.grades.isEmpty(),
        rightColumnContent = {
            val average = remember { subject.grades.average() }

            if (subject.finalGrade == null)
                GradeAverageComposable(average)
            else
            {
                var showAverage by rememberMutableStateOf(false)

                if (!showAverage)
                    GradeComposable(
                        grade = Grade(subject.finalGrade!!.value, false),
                        onClick = { showAverage = true }
                    )
                else
                    GradeAverageComposable(
                        value = average,
                        onClick = { showAverage = false }
                    )
            }
        }
    ) {
        if (subject.grades.isEmpty())
            Text(stringResource(R.string.no_grades))
        else
        {
            Column {
                subject.grades.subjectParts.forEach { subjectPart ->
                    SubjectPart(subjectPart, subject.grades[subjectPart]!!, onGradeClick)
                }
            }
        }
    }
}

@Composable
private fun SubjectPart(
    subjectPart: String? = null,
    grades: List<Grade>,
    onGradeClick: (Grade) -> Unit = {}
)
{
    if (subjectPart != null)
        Text("$subjectPart:", Modifier.padding(vertical = 10.dp))

    FlowRow(
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        mainAxisSpacing = 5.dp,
        crossAxisSpacing = 5.dp
    ) {
        grades.forEach {
            GradeComposable(it, onClick = { onGradeClick(it) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeComposable(
    grade: Grade,
    onClick: () -> Unit
)
{
    val gradeHeight = if (grade.small) Constants.gradeSmallHeight else Constants.gradeWidth
    val gradeColor = remember { getGradeColor(grade) }

    /* Using Surface here for dropping th shadow. */
    Surface(
        modifier = Modifier.size(Constants.gradeWidth, gradeHeight),
        onClick = onClick,
        color = gradeColor,
        shape = Constants.gradeShape,
        shadowElevation = Constants.gradeShadowElevation
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = grade.valueChar().toString(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun GradeComposable(grade: Grade)
{
    val gradeHeight = if (grade.small) Constants.gradeSmallHeight else Constants.gradeWidth
    val gradeColor = remember { getGradeColor(grade) }

    /* Using Surface here for dropping th shadow. */
    Surface(
        modifier = Modifier.size(Constants.gradeWidth, gradeHeight),
        color = gradeColor,
        shape = Constants.gradeShape,
        shadowElevation = Constants.gradeShadowElevation
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = grade.valueChar().toString(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

/*@Composable
private fun GradeComposable(
    grade: Grade,
    onClick: () -> Unit
)
{
    val gradeHeight = if (grade.small) Constants.gradeSmallHeight else Constants.gradeWidth
    val gradeColor = remember { getGradeColor(grade) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
                .size(Constants.gradeWidth, gradeHeight)
                .clip(RoundedCornerShape(7.dp))
                .background(gradeColor)
                .clickable(onClick = onClick)
    ) {
        Text(
            grade.valueChar().toString(),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeAverageComposable(
    value: Float,
    onClick: () -> Unit
)
{
    val valueString = remember {
        Constants.gradeAverageDecimalFormat.format(value)
    }

    Surface(
        modifier = Modifier.size(Constants.gradeWidth),
        border = BorderStroke(1.dp, getGradeColor(value.roundToInt())),
        tonalElevation = 10.dp,
        shape = Constants.gradeShape,
        shadowElevation = Constants.gradeShadowElevation,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = valueString,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun GradeAverageComposable(value: Float)
{
    val valueString = remember {
        Constants.gradeAverageDecimalFormat.format(value)
    }

    Surface(
        modifier = Modifier.size(Constants.gradeWidth),
        border = BorderStroke(1.dp, getGradeColor(value.roundToInt())),
        tonalElevation = 10.dp,
        shape = Constants.gradeShape,
        shadowElevation = Constants.gradeShadowElevation
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = valueString,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun GradeDialog(grade: Grade, onDismiss: () -> Unit)
{
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 5.dp,
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(2.dp, getGradeColor(grade))
        ) {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    color = getGradeColor(grade),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getGradeWord(grade),
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (grade.receiveDate != null)
                        GradeDialogRow(grade.receiveDate!!.format(Constants.gradeDateFormatter)!!)
                    if (grade.description != null)
                        GradeDialogRow(grade.description!!)
                    if (grade.teacher != null)
                        GradeDialogRow(grade.teacher!!)
                }
            }
        }
    }
}

@Composable
private fun GradeDialogRow(value: String)
{
    Surface(
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = value,
            modifier = Modifier.padding(15.dp).fillMaxWidth()
        )
    }
}

@Composable
private fun Behaviour(behaviour: Behaviour)
{
    Container(
        modifier = Modifier.fillMaxWidth(),
        title = Behaviour.SUBJECT_NAME,
        rightColumnContent = {
            GradeComposable(
                grade = Grade(behaviour.finalGrade.value, false)
            )
        }
    ) {
        if (behaviour.notifications.isEmpty())
            Text(stringResource(R.string.no_grades))
        else
        {
            FlowRow(
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
                mainAxisSpacing = 5.dp,
                crossAxisSpacing = 5.dp
            ) {
                behaviour.notifications.forEach {
                    BehaviourNotification(it)
                }
            }
        }
    }
}

@Composable
private fun BehaviourNotification(behaviourNotification: Behaviour.Notification)
{
    Surface(
        tonalElevation = 10.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(7.dp)
    ) {
        Row(Modifier.padding(5.dp)) {
            Text(
                modifier = Modifier.padding(end = 5.dp),
                text = behaviourNotification.message
            )
            when (behaviourNotification.type)
            {
                Behaviour.NotificationType.GOOD ->
                    Image(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(getGradeColor(1))
                    )
                Behaviour.NotificationType.BAD  ->
                    Image(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(getGradeColor(5))
                    )
            }
        }
    }
}

@Composable
private fun getGradeWord(grade: Grade) = when (grade.value)
{
    0    -> stringResource(R.string.grade_word_0)
    1    -> stringResource(R.string.grade_word_1)
    2    -> stringResource(R.string.grade_word_2)
    3    -> stringResource(R.string.grade_word_3)
    4    -> stringResource(R.string.grade_word_4)
    5    -> stringResource(R.string.grade_word_5)
    else -> throw IllegalArgumentException("Grade value must be between 0 and 5. (got ${grade.value})")
}

private object Constants
{
    val gradeWidth = 40.dp
    val gradeSmallHeight = 25.dp
    val gradeAverageDecimalFormat = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.HALF_UP
    }
    val gradeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    val gradeShadowElevation = 2.dp
    val gradeShape = RoundedCornerShape(7.dp)
}
