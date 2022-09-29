package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.grade.Behaviour
import me.tomasan7.jecnaapi.data.grade.Grade
import me.tomasan7.jecnaapi.data.grade.Subject
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.GradesViewModel
import me.tomasan7.jecnamobile.ui.component.DialogRow
import me.tomasan7.jecnamobile.ui.component.SchoolYearHalfSelector
import me.tomasan7.jecnamobile.ui.component.SchoolYearSelector
import me.tomasan7.jecnamobile.ui.component.VerticalDivider
import me.tomasan7.jecnamobile.ui.theme.jm_label
import me.tomasan7.jecnamobile.util.getGradeColor
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@SubScreensNavGraph
@Destination
@Composable
fun GradesSubScreen(
    viewModel: GradesViewModel = hiltViewModel()
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
                SchoolYearHalfSelector(
                    modifier = Modifier.width(160.dp),
                    selectedSchoolYearHalf = uiState.selectedSchoolYearHalf,
                    onChange = { viewModel.selectSchoolYearHalf(it) }
                )
            }

            if (uiState.gradesPage != null)
            {
                uiState.subjectsSorted!!.forEach { subject ->
                    key(subject) {
                        Subject(
                            subject = subject,
                            onGradeClick = { showDialog(it) }
                        )
                    }
                }

                Behaviour(uiState.gradesPage.behaviour)
            }
        }

        /* Show the grade dialog. */
        if (dialogOpened && dialogGrade != null)
            GradeDialog(dialogGrade!!, ::hideDialog)
    }
}

@Composable
private fun Container(
    title: @Composable () -> Unit = {},
    rightColumnVisible: Boolean = true,
    rightColumnContent: @Composable ColumnScope.() -> Unit = {},
    onRightColumnClick: (() -> Unit)? = null,
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
        Row(Modifier
                    .padding(20.dp)
                    .onSizeChanged { rowHeightValue = it.height }
        ) {
            Column(Modifier.weight(1f, true)) {
                title()

                Spacer(Modifier.height(15.dp))

                content()
            }

            if (rightColumnVisible)
            {
                VerticalDivider(
                    modifier = Modifier.rowHeight().padding(horizontal = 10.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
                )

                Column(
                    modifier = Modifier.rowHeight().clickable(
                        enabled = onRightColumnClick != null,
                        onClick = { onRightColumnClick?.invoke() },
                        indication = null,
                        interactionSource = MutableInteractionSource()
                    ),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    content = rightColumnContent
                )
            }
        }
    }
}

@Composable
private fun Container(
    title: String,
    rightColumnVisible: Boolean = true,
    rightColumnContent: @Composable ColumnScope.() -> Unit = {},
    onRightColumnClick: (() -> Unit)? = null,
    modifier: Modifier,
    content: @Composable () -> Unit = {}
) = Container(
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    },
    rightColumnVisible = rightColumnVisible,
    rightColumnContent = rightColumnContent,
    onRightColumnClick = onRightColumnClick,
    modifier = modifier,
    content = content
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Subject(
    subject: Subject,
    onGradeClick: (Grade) -> Unit = {}
)
{
    val average = remember { subject.grades.average() }
    var showAverage by rememberMutableStateOf(false)

    Container(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Column {
                Text(subject.name.full, style = MaterialTheme.typography.titleMedium)
                if (subject.grades.count != 0)
                    Text(
                        text = pluralStringResource(R.plurals.grades_count, subject.grades.count, subject.grades.count),
                        color = jm_label,
                        fontSize = 10.sp
                    )
            }
        },
        rightColumnVisible = subject.finalGrade != null || average != null,
        rightColumnContent = {
            if (subject.finalGrade == null)
                GradeAverageComposable(average!!)
            else
                if (!showAverage)
                    GradeComposable(Grade(subject.finalGrade!!.value, false))
                else
                    GradeAverageComposable(average!!)
        },
        onRightColumnClick = { showAverage = !showAverage }
    ) {
        if (subject.grades.isEmpty())
            Text(
                text = stringResource(R.string.no_grades),
                fontSize = 14.sp
            )
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
                    DialogRow(stringResource(R.string.grade_receive_date), grade.receiveDate?.format(Constants.gradeDateFormatter) ?: "")
                    DialogRow(stringResource(R.string.grade_description), grade.description ?: "")
                    DialogRow(stringResource(R.string.grade_teacher), grade.teacher?.full ?: "")
                }
            }
        }
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
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = getGradeColor(1)
                    )
                Behaviour.NotificationType.BAD  ->
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = getGradeColor(5)
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