package me.tomasan7.jecnamobile.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import de.palm.composestateevents.EventEffect
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.component.SubScreenTopAppBar
import me.tomasan7.jecnamobile.ui.component.VerticalSpacer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SubScreensNavGraph
@Destination
@Composable
fun TeachersSubScreen(
    onHamburgerClick: () -> Unit,
    viewModel: TeachersViewModel = hiltViewModel()
)
{
    DisposableEffect(Unit) {
        viewModel.enteredComposition()
        onDispose {
            viewModel.leftComposition()
        }
    }

    val uiState = viewModel.uiState
    val pullRefreshState = rememberPullRefreshState(uiState.loading, viewModel::reload)
    val snackbarHostState = remember { SnackbarHostState() }

    EventEffect(
        event = uiState.snackBarMessageEvent,
        onConsumed = viewModel::onSnackBarMessageEventConsumed
    ) {
        snackbarHostState.showSnackbar(it)
    }

    Scaffold(
        topBar = { SubScreenTopAppBar(R.string.sidebar_teachers, onHamburgerClick = onHamburgerClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                FilterFieldRow(
                    value = uiState.filterFieldValue,
                    onValueChange = viewModel::onFilterFieldValueChange
                )

                VerticalSpacer(16.dp)

                uiState.teacherReferencesSortedFiltered?.forEach {
                    TeacherCard(
                        teacher = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                VerticalSpacer(16.dp)
            }

            PullRefreshIndicator(
                refreshing = uiState.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterFieldRow(
    value: String = "",
    onValueChange: (String) -> Unit = {}
)
{
    val trailingIcon: (@Composable () -> Unit)? = remember(value) {
        if (value.isNotEmpty())
        {
            {
                IconButton({ onValueChange("") }) {
                    Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
                }
            }
        }
        else
            null
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier,
            label = { Text(stringResource(R.string.teachers_filter)) },
            value = value,
            trailingIcon = trailingIcon,
            onValueChange = onValueChange,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCard(
    teacher: TeacherReference,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)
{
    Surface(
        tonalElevation = 4.dp,
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(teacher.fullName)
            Text(teacher.tag)
        }
    }
}