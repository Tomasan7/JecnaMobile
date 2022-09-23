package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.CanteenViewModel
import me.tomasan7.jecnamobile.ui.component.Card
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_ordered
import me.tomasan7.jecnamobile.util.getWeekDayName
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import java.time.format.DateTimeFormatter

@SubScreensNavGraph
@Destination
@Composable
fun CanteenSubScreen(
    viewModel: CanteenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    var dialogOpened by rememberMutableStateOf(false)
    var dialogDayMenu by rememberMutableStateOf<DayMenu?>(null)

    fun showDialog(dayMenu: DayMenu)
    {
        dialogOpened = true
        dialogDayMenu = dayMenu
    }

    fun hideDialog()
    {
        dialogOpened = false
        dialogDayMenu = null
    }

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadMenu() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.menu != null)
            {
                uiState.futureDayMenusSorted!!.forEach { dayMenu ->
                    key(dayMenu) {
                        DayMenu(
                            dayMenu = dayMenu,
                            onMenuItemClick = { viewModel.orderMenuItem(it) },
                            onInfoClick = { showDialog(dayMenu) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.orderInProcess)
        Dialog(onDismissRequest = { }) {
            CircularProgressIndicator()
        }

    /* Show the grade dialog. */
    if (dialogOpened && dialogDayMenu != null)
        DayMenuDialog(dialogDayMenu!!, ::hideDialog)
}

@Composable
private fun DayMenu(
    dayMenu: DayMenu,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {},
    onMenuItemClick: (MenuItem) -> Unit = {}
)
{
    val dayName = getWeekDayName(dayMenu.day.dayOfWeek)
    val dayDate = remember { dayMenu.day.format(DATE_FORMATTER) }
    val isSoupSameForAllItems = remember {
        val firstItem = dayMenu.items.firstOrNull() ?: return@remember false
        dayMenu.items.all { it.description.soup == firstItem.description.soup }
    }

    Card(
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$dayName $dayDate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                )

                Icon(Icons.Outlined.Info, null, Modifier.clip(CircleShape).clickable(onClick = onInfoClick))
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isSoupSameForAllItems)
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        tonalElevation = 10.dp,
                        shadowElevation = 2.dp,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(dayMenu.items.first().description.soup!!, Modifier.padding(5.dp))
                    }
                }

            dayMenu.items.forEach { menuItem ->
                key(menuItem) {
                    MenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        menuItem = menuItem,
                        onClick = { onMenuItemClick(menuItem) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItem(
    menuItem: MenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)
{
    Surface(
        tonalElevation = 10.dp,
        /* Semi-transparent background and shadow don't go together */
        shadowElevation = if (!menuItem.ordered && jm_canteen_ordered.alpha != 1f) 2.dp else 0.dp,
        modifier = modifier,
        color = if (menuItem.ordered) jm_canteen_ordered else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = menuItem.description.rest.replaceFirstChar { it.uppercase() }.replace(" , ", ", "),
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun DayMenuDialog(dayMenu: DayMenu, onDismiss: () -> Unit)
{
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 5.dp,
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = stringResource(R.string.canteen_allergens), style = MaterialTheme.typography.titleMedium)

                dayMenu.items.forEach {  menuItem ->
                    Surface(tonalElevation = 10.dp, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(5.dp)) {
                        Text(text = menuItem.allergens.joinToString(), textAlign = TextAlign.Center, modifier = Modifier.padding(10.dp).fillMaxWidth())
                    }
                }
            }
        }
    }
}

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.")