package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.CanteenViewModel
import me.tomasan7.jecnamobile.ui.component.Card
import me.tomasan7.jecnamobile.util.getWeekDayName
import java.time.format.DateTimeFormatter

@SubScreensNavGraph
@Destination
@Composable
fun CanteenSubScreen(
    viewModel: CanteenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

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
                            onMenuItemClick = { viewModel.orderMenuItem(it) }
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
}

@Composable
private fun DayMenu(
    dayMenu: DayMenu,
    modifier: Modifier = Modifier,
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
            Text(
                text = "$dayName $dayDate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            dayMenu.items.forEach { menuItem ->
                key(menuItem) {
                    MenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        menuItem = menuItem,
                        onClick = { onMenuItemClick(menuItem) }
                    )
                }
            }

            if (isSoupSameForAllItems)
            {
                Text(dayMenu.items.first().description.soup!!)
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
        shadowElevation = 2.dp,
        modifier = modifier,
        border = if (menuItem.ordered) BorderStroke(1.dp, MaterialTheme.colorScheme.inverseSurface) else null,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(menuItem.description.rest, Modifier.padding(10.dp))
    }
}

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.")