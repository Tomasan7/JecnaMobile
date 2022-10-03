package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.*
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.SettingsViewModel
import me.tomasan7.jecnamobile.ui.component.FilledDropDownSelector
import me.tomasan7.jecnamobile.ui.component.RadioGroup
import me.tomasan7.jecnamobile.ui.theme.jm_label
import me.tomasan7.jecnamobile.util.Settings
import me.tomasan7.jecnamobile.util.Theme


@SubScreensNavGraph
@Destination
@Composable
fun SettingsSubScreen(
    viewModel: SettingsViewModel = hiltViewModel()
)
{
    val names = mapOf(
        NewsSubScreenDestination.route to stringResource(R.string.sidebar_news),
        GradesSubScreenDestination.route to stringResource(R.string.sidebar_grades),
        TimetableSubScreenDestination.route to stringResource(R.string.sidebar_timetable),
        CanteenSubScreenDestination.route to stringResource(R.string.sidebar_canteen),
        AttendancesSubScreenDestination.route to stringResource(R.string.sidebar_attendances)
    )

    val settings by viewModel.settingsDataStore.data.collectAsState(initial = Settings())

    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.headlineMedium
        )

        val themeOptionStringsArray = stringArrayResource(R.array.settings_theme_options)
        RadioGroup(
            options = Theme.values().toList(),
            optionStringMap = { themeOptionStringsArray[it.ordinal] },
            selectedOption = settings.theme,
            onSelectionChange = { viewModel.setTheme(theme = it) }
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.settings_open_subscreen_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Column(modifier = Modifier.padding(start = 10.dp)) {

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.settings_open_subscreen_description),
                color = jm_label,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(10.dp))

            FilledDropDownSelector(
                options = names.keys.toList(),
                optionStringMap = { names[it]!! },
                selectedValue = settings.openSubScreenRoute,
                onChange = { viewModel.setOpenSubScreen(it) }
            )
        }
    }
}