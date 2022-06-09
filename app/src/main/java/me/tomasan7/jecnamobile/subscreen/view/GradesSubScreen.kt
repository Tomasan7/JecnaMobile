package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import me.tomasan7.jecnaapi.data.Grade
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.viewmodel.GradesSubScreenViewModel
import me.tomasan7.jecnamobile.util.getGradeColor

@Composable
fun GradesSubScreen(modifier: Modifier = Modifier)
{
    val viewModel = viewModel<GradesSubScreenViewModel>()
    val uiState = viewModel.uiState
    var openDialog by remember { mutableStateOf(false) }
    var dialogGrade by remember { mutableStateOf<Grade?>(null) }

    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        Arrangement.spacedBy(20.dp)
    ) {
        uiState.subjects.forEach { subject ->
            Subject(subject.name, subject.grades, { openDialog = true; dialogGrade = it }, Modifier.fillMaxWidth())
        }

        if (openDialog && dialogGrade != null)
            GradeDialog(dialogGrade!!) { openDialog = false }
    }
}

@Composable
private fun Subject(
    name: String,
    grades: List<Grade>,
    onGradeClick: (Grade) -> Unit,
    modifier: Modifier = Modifier
)
{
    Surface(
        modifier,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(name, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(15.dp))
            FlowRow(
                Modifier.fillMaxWidth(),
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
                mainAxisSpacing = 5.dp,
                crossAxisSpacing = 5.dp
            ) {
                grades.forEach {
                    GradeComposable(it, onClick = { onGradeClick(it) })
                }
            }
        }
    }
}

@Composable
private fun GradeComposable(
    grade: Grade,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    val gradeWidth = 40.dp
    val gradeHeight = if (grade.small) 25.dp else gradeWidth

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Canvas(Modifier.width(gradeWidth).height(gradeHeight)) {
            drawRoundRect(color = getGradeColor(grade),
                          size = size,
                          cornerRadius = CornerRadius(20f, 20f))
        }
        Text(
            grade.valueChar().toString(),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
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
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(getGradeWord(grade), color = Color.Black, fontSize = 20.sp)
                    }
                }
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GradeDialogRow("${stringResource(R.string.grade_weight)}: ",
                                   if (grade.small) stringResource(R.string.grade_weight_small) else stringResource(R.string.grade_weight_big))
                    if (grade.description != null)
                        GradeDialogRow("${stringResource(R.string.grade_description)}: ", grade.description!!, true)
                    if (grade.teacher != null)
                        GradeDialogRow("${stringResource(R.string.grade_teacher)}: ", grade.teacher!!, true)
                }
            }
        }
    }
}

@Composable
private fun GradeDialogRow(key: String, value: String, valueNewLine: Boolean = false)
{
    Surface(
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.padding(15.dp).fillMaxWidth()) {
            val content = @Composable {
                Text(key)
                Text(value)
            }

            if (valueNewLine)
                Column(Modifier.fillMaxWidth(), content = { content() })
            else
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    content = { content() }
                )
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