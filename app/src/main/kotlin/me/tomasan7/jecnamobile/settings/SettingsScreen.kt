package me.tomasan7.jecnamobile.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import me.tomasan7.jecnamobile.ui.component.HorizontalSpacer
import me.tomasan7.jecnamobile.ui.component.RadioGroup
import me.tomasan7.jecnamobile.ui.component.VerticalSpacer
import me.tomasan7.jecnamobile.ui.theme.jm_label
import me.tomasan7.jecnamobile.util.settingsAsState
import kotlin.math.roundToInt


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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Option(
            title = stringResource(id = R.string.settings_theme_title)
        ) {
            val themeOptionStringsArray = stringArrayResource(R.array.settings_theme_options)

            RadioGroup(
                options = Settings.Theme.values().toList(),
                optionStringMap = { themeOptionStringsArray[it.ordinal] },
                selectedOption = settings.theme,
                onSelectionChange = { viewModel.setTheme(theme = it) }
            )
        }

        Option(
            title = stringResource(id = R.string.settings_open_subscreen_title),
            description = stringResource(id = R.string.settings_open_subscreen_description)
        ) {
            FilledDropDownSelector(
                options = names.keys.toList(),
                optionStringMap = { names[it]!! },
                selectedValue = settings.openSubScreenRoute,
                onChange = { viewModel.setOpenSubScreen(it) }
            )
        }

        Option(
            title = stringResource(R.string.settings_canteen_image_tolerance_title),
            description = stringResource(R.string.settings_canteen_image_tolerance_description)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = settings.canteenImageTolerance,
                    onValueChange = { viewModel.setCanteenImageTolerance(it) },
                    valueRange = 0f..1f,
                    steps = 100,
                )
                Text(
                    text = "${(settings.canteenImageTolerance * 100).roundToInt()} %",
                    color = jm_label,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun Option(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
)
{
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )

        if (description != null)
            Text(
                text = description,
                color = jm_label,
                style = MaterialTheme.typography.bodyMedium
            )

        content()
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