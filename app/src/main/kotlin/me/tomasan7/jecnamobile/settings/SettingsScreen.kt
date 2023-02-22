package me.tomasan7.jecnamobile.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.*
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.component.FilledDropDownSelector
import me.tomasan7.jecnamobile.ui.component.RadioGroup
import me.tomasan7.jecnamobile.ui.component.VerticalSpacer
import me.tomasan7.jecnamobile.ui.theme.jm_label
import me.tomasan7.jecnamobile.util.settingsAsState


@OptIn(ExperimentalMaterial3Api::class)
@SubScreensNavGraph
@Destination
@Composable
fun SettingsScreen(
    navigator: DestinationsNavigator,
    viewModel: SettingsViewModel = hiltViewModel()
)
{
    Scaffold(
        topBar = { TopAppBar(navigator::popBackStack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Settings(viewModel)
        }
    }
}

@Composable
private fun Settings(viewModel: SettingsViewModel)
{
    val names = mapOf(
        NewsSubScreenDestination.route to stringResource(R.string.sidebar_news),
        GradesSubScreenDestination.route to stringResource(R.string.sidebar_grades),
        TimetableSubScreenDestination.route to stringResource(R.string.sidebar_timetable),
        TeachersSubScreenDestination.route to stringResource(R.string.sidebar_teachers),
        AttendancesSubScreenDestination.route to stringResource(R.string.sidebar_attendances)
    )

    val settings by settingsAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.settings_theme_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )

        val themeOptionStringsArray = stringArrayResource(R.array.settings_theme_options)

        RadioGroup(
            options = Settings.Theme.values().toList(),
            optionStringMap = { themeOptionStringsArray[it.ordinal] },
            selectedOption = settings.theme,
            onSelectionChange = { viewModel.setTheme(theme = it) }
        )

        VerticalSpacer(10.dp)

        Text(
            text = stringResource(R.string.settings_open_subscreen_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {

            VerticalSpacer(10.dp)

            Text(
                text = stringResource(R.string.settings_open_subscreen_description),
                color = jm_label,
                style = MaterialTheme.typography.bodyMedium
            )

            VerticalSpacer(10.dp)

            FilledDropDownSelector(
                options = names.keys.toList(),
                optionStringMap = { names[it]!! },
                selectedValue = settings.openSubScreenRoute,
                onChange = { viewModel.setOpenSubScreen(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    onBackClick: () -> Unit = {},
)
{
    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.sidebar_settings)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}