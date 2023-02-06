package me.tomasan7.jecnamobile.grades

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.ramcosta.composedestinations.annotation.Destination
import de.palm.composestateevents.EventEffect
import me.tomasan7.jecnaapi.data.grade.*
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.component.*
import me.tomasan7.jecnamobile.ui.theme.*
import me.tomasan7.jecnamobile.util.getGradeColor
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SubScreensNavGraph(start = true)
@Destination
@Composable
fun GradesSubScreen(
    onHamburgerClick: () -> Unit,
    viewModel: GradesViewModel = hiltViewModel()
)
{
    DisposableEffect(Unit) {
        viewModel.enteredComposition()
        onDispose {
            viewModel.leftComposition()
        }
    }

    val uiState = viewModel.uiState
    val objectDialogState = rememberObjectDialogState<Grade>()
    val pullRefreshState = rememberPullRefreshState(uiState.loading, viewModel::reload)
    val snackbarHostState = remember { SnackbarHostState() }
    
    EventEffect(
        event = uiState.snackBarMessageEvent,
        onConsumed = viewModel::onSnackBarMessageEventConsumed
    ) {
        snackbarHostState.showSnackbar(it)
    }

    Scaffold(
        topBar = { SubScreenTopAppBar(R.string.sidebar_grades, onHamburgerClick = onHamburgerClick) },
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
                    selectedSchoolYear = uiState.selectedSchoolYear,
                    selectedSchoolYearHalf = uiState.selectedSchoolYearHalf,
                    onChangeSchoolYear = { viewModel.selectSchoolYear(it) },
                    onChangeSchoolYearHalf = { viewModel.selectSchoolYearHalf(it) }
                )

                if (uiState.gradesPage != null)
                {
                    uiState.subjectsSorted!!.forEach { subject ->
                        key(subject) {
                            Subject(
                                subject = subject,
                                onGradeClick = { objectDialogState.show(it) }
                            )
                        }
                    }

                    Behaviour(uiState.gradesPage.behaviour)
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            ObjectDialog(
                state = objectDialogState,
                onDismissRequest = { objectDialogState.hide() },
                content = { grade -> GradeDialogContent(grade = grade) }
            )
        }
    }
}

@Composable
private fun PeriodSelectors(
    selectedSchoolYear: SchoolYear,
    selectedSchoolYearHalf: SchoolYearHalf,
    onChangeSchoolYear: (SchoolYear) -> Unit,
    onChangeSchoolYearHalf: (SchoolYearHalf) -> Unit,
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
        SchoolYearHalfSelector(
            modifier = Modifier.width(160.dp),
            selectedSchoolYearHalf = selectedSchoolYearHalf,
            onChange = onChangeSchoolYearHalf
        )
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
        Row(
            Modifier
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
                    modifier = Modifier
                        .rowHeight()
                        .padding(horizontal = 10.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
                )

                Column(
                    modifier = Modifier
                        .rowHeight()
                        .clickable(
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
                        style = MaterialTheme.typography.labelSmall
                    )
            }
        },
        rightColumnVisible = subject.finalGrade != null || average != null,
        rightColumnContent = {
            if (subject.finalGrade == null)
                GradeAverage(average!!)
            else
                if (showAverage)
                    GradeAverage(average!!)
                else
                    FinalGrade(finalGrade = subject.finalGrade!!)

        },
        onRightColumnClick = { showAverage = !showAverage }
    ) {
        if (subject.grades.isEmpty())
            Text(
                text = stringResource(R.string.no_grades),
                style = MaterialTheme.typography.bodyMedium
            )
        else
            Column {
                subject.grades.subjectParts.forEach { subjectPart ->
                    SubjectPart(subjectPart, subject.grades[subjectPart]!!, onGradeClick)
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
        Text(
            text = "$subjectPart:",
            modifier = Modifier.padding(vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge
        )

    FlowRow(
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        mainAxisSpacing = 5.dp,
        crossAxisSpacing = 5.dp
    ) {
        grades.forEach {
            Grade(it, onClick = { onGradeClick(it) })
        }
    }
}

@Composable
private fun GradeBox(
    text: String,
    color: Color,
    height: Dp = Constants.gradeWidth,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
{
    var newModifier = modifier
        .size(Constants.gradeWidth, height)
        .shadow(Constants.gradeShadowElevation, Constants.gradeShape, true)
        .background(color)

    if (onClick != null)
        newModifier = newModifier.clickable(onClick = onClick)

    /* Using Surface here for dropping the shadow. */
    Box(
        modifier = newModifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun Grade(
    grade: Grade,
    onClick: (() -> Unit)? = null
)
{
    val gradeHeight = if (grade.small) Constants.gradeSmallHeight else Constants.gradeWidth
    val gradeColor = remember { getGradeColor(grade) }

    GradeBox(
        text = grade.valueChar().toString(),
        color = gradeColor,
        height = gradeHeight,
        onClick = onClick
    )
}

@Composable
private fun GradeAverage(value: Float)
{
    val valueString = remember { Constants.gradeAverageDecimalFormat.format(value) }

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
private fun FinalGrade(finalGrade: FinalGrade) = when (finalGrade)
{
    is FinalGrade.Grade                   -> Grade(Grade(finalGrade.value, false))
    is FinalGrade.GradesWarning           -> GradesWarning()
    is FinalGrade.AbsenceWarning          -> AbsenceWarning()
    is FinalGrade.GradesAndAbsenceWarning -> GradesAndAbsenceWarning()
}


@Composable
private fun GradesWarning()
{
    GradeBox(
        text = stringResource(R.string.grade_grades_warning_content),
        color = grade_grades_warning
    )
}

@Composable
private fun AbsenceWarning()
{
    GradeBox(
        text = stringResource(R.string.grade_absence_warning_content),
        color = grade_absence_warning
    )
}

@Composable
private fun GradesAndAbsenceWarning()
{
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        GradesWarning()
        AbsenceWarning()
    }
}

@Composable
private fun GradeDialogContent(grade: Grade)
{
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, getGradeColor(grade))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(getGradeColor(grade))
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getGradeWord(grade),
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DialogRow(
                    stringResource(R.string.grade_receive_date),
                    grade.receiveDate?.format(Constants.gradeDateFormatter) ?: ""
                )
                DialogRow(stringResource(R.string.grade_description), grade.description ?: "")
                DialogRow(stringResource(R.string.grade_teacher), grade.teacher?.full ?: "")
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
            if (behaviour.finalGrade is FinalGrade.Grade)
                Grade(Grade((behaviour.finalGrade as FinalGrade.Grade).value, false))
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
        shadowElevation = Constants.gradeShadowElevation,
        shape = RoundedCornerShape(7.dp)
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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