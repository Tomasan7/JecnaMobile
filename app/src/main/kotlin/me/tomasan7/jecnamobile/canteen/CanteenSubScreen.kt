package me.tomasan7.jecnamobile.canteen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import de.palm.composestateevents.EventEffect
import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.ElevationLevel
import me.tomasan7.jecnamobile.ui.component.*
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_disabled
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_ordered
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_ordered_disabled
import me.tomasan7.jecnamobile.util.getWeekDayName
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@SubScreensNavGraph
@Destination
@Composable
fun CanteenSubScreen(
    onHamburgerClick: () -> Unit = {},
    viewModel: CanteenViewModel = hiltViewModel()
)
{
    DisposableEffect(Unit) {
        viewModel.enteredComposition()
        onDispose {
            viewModel.leftComposition()
        }
    }

    val uiState = viewModel.uiState
    val menuItemDialogState = rememberObjectDialogState<MenuItemWithNumber>()
    val allergensDialogState = rememberObjectDialogState<DayMenu>()
    val pullRefreshState = rememberPullRefreshState(uiState.loading, viewModel::reload)
    val snackbarHostState = remember { SnackbarHostState() }

    EventEffect(
        event = uiState.snackBarMessageEvent,
        onConsumed = viewModel::onSnackBarMessageEventConsumed
    ) {
        snackbarHostState.showSnackbar(it)
    }

    Scaffold(
        topBar = {
            SubScreenTopAppBar(
                R.string.sidebar_canteen,
                onHamburgerClick = onHamburgerClick,
                actions = {
                    if (uiState.menuPage != null)
                        Credit(uiState.menuPage.credit)
                }
            )
        },
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
                if (uiState.menuPage != null)
                {
                    uiState.futureDayMenusSorted!!.forEach { dayMenu ->
                        key(dayMenu) {
                            DayMenu(
                                dayMenu = dayMenu,
                                onMenuItemClick = { menuItemDialogState.show(it) },
                                onInfoClick = { allergensDialogState.show(dayMenu) }
                            )
                        }
                    }

                    /* 0 because the space is already created by the Column, because of Arrangement.spacedBy() */
                    Spacer(Modifier.height(0.dp))
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            ObjectDialog(
                state = menuItemDialogState,
                onDismissRequest = { menuItemDialogState.hide() },
                content = { menuItemWithNumber ->
                    MenuItemDialogContent(
                        menuItemWithNumber = menuItemWithNumber,
                        onOrderClick = {
                            viewModel.orderMenuItem(
                                menuItemWithNumber.menuItem
                            ); menuItemDialogState.hide()
                        },
                        onPutOnExchangeClick = {
                            viewModel.putMenuItemOnExchange(
                                menuItemWithNumber.menuItem
                            ); menuItemDialogState.hide()
                        },
                        onCloseCLick = { menuItemDialogState.hide() }
                    )
                }
            )

            ObjectDialog(
                state = allergensDialogState,
                onDismissRequest = { allergensDialogState.hide() },
                content = { dayMenu ->
                    AllergensDialogContent(
                        dayMenu = dayMenu,
                        onCloseCLick = { allergensDialogState.hide() })
                }
            )

            if (uiState.orderInProcess)
                Dialog(onDismissRequest = { }) {
                    CircularProgressIndicator()
                }
        }
    }
}

@Composable
private fun DayMenu(
    dayMenu: DayMenu,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {},
    onMenuItemClick: (MenuItemWithNumber) -> Unit = {}
)
{
    val dayName = getWeekDayName(dayMenu.day.dayOfWeek)
    val dayDate = remember { dayMenu.day.format(DATE_FORMATTER) }
    val isSoupSameForAllItems = remember {
        val firstItem = dayMenu.items.firstOrNull() ?: return@remember false
        dayMenu.items.all { it.description?.soup == firstItem.description?.soup }
    }

    Card(
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$dayName $dayDate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                )

                val anyMenuItemsWithAllergens = dayMenu.items.any { it.allergens != null }

                if (anyMenuItemsWithAllergens)
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, null)
                    }
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSoupSameForAllItems)
                dayMenu.items.firstOrNull()?.description?.soup?.let { Soup(it) }

            dayMenu.items.forEachIndexed { index, menuItem ->
                val menuItemWithNumber = MenuItemWithNumber(menuItem, index + 1)

                key(menuItem) {
                    MenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        menuItemWithNumber = menuItemWithNumber,
                        onClick = { onMenuItemClick(menuItemWithNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Soup(soup: String)
{
    Surface(
        tonalElevation = ElevationLevel.level3,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_soup_filled),
                contentDescription = null
            )
            Text(soup, Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
private fun Credit(credit: Float)
{
    Icon(
        imageVector = Icons.Filled.AccountBalanceWallet,
        contentDescription = null,
    )

    val creditNumberStr = remember(credit) {
        String.format(if (credit.rem(1) == 0f) "%.0f" else "%.2f", credit)
    }

    Text(
        text = stringResource(R.string.canteen_credit, creditNumberStr),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItem(
    menuItemWithNumber: MenuItemWithNumber,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)
{
    val menuItem = menuItemWithNumber.menuItem

    val color = when
    {
        menuItem.isOrdered && !menuItem.isEnabled -> jm_canteen_ordered_disabled
        menuItem.isOrdered                        -> jm_canteen_ordered
        !menuItem.isEnabled                       -> jm_canteen_disabled
        else                                      -> MaterialTheme.colorScheme.surface
    }

    val lunchString = stringResource(R.string.canteen_lunch, menuItemWithNumber.number)

    val text = remember(menuItem.description?.rest) {
        menuItem.description?.rest?.replaceFirstChar { it.uppercase() }?.replace(" , ", ", ")
            ?: lunchString
    }

    Surface(
        tonalElevation = 10.dp,
        modifier = modifier,
        color = color,
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )

            if (menuItem.isInExchange)
                Icon(
                    modifier = Modifier.padding(start = 10.dp),
                    imageVector = Icons.Filled.TrendingUp,
                    tint = Color.Gray.copy(alpha = .5f),
                    contentDescription = null
                )
        }
    }
}

@Composable
private fun AllergensDialogContent(
    dayMenu: DayMenu,
    onCloseCLick: () -> Unit = {}
)
{
    DialogContainer(
        title = {
            Text(stringResource(R.string.canteen_allergens))
        },
        buttons = {
            TextButton(onClick = onCloseCLick) {
                Text(stringResource(R.string.close))
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dayMenu.items.forEachIndexed { index, menuItem ->
                AllergensForMenuItem(MenuItemWithNumber(menuItem, index + 1))
            }
        }
    }
}

@Composable
private fun AllergensForMenuItem(menuItemWithNumber: MenuItemWithNumber)
{
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.canteen_lunch, menuItemWithNumber.number))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = ElevationLevel.level5,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = menuItemWithNumber.menuItem.allergens?.joinToString() ?: "",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MenuItemDialogContent(
    menuItemWithNumber: MenuItemWithNumber,
    onOrderClick: () -> Unit = {},
    onPutOnExchangeClick: () -> Unit = {},
    onCloseCLick: () -> Unit = {}
)
{
    val menuItem = menuItemWithNumber.menuItem

    DialogContainer(
        title = {
            Text(stringResource(R.string.canteen_lunch, menuItemWithNumber.number))
        },
        buttons = {
            TextButton(onClick = onCloseCLick) {
                Text(stringResource(R.string.close))
            }

            if (menuItem.putOnExchangePath != null)
                Button(onClick = onPutOnExchangeClick) {
                    Text(
                        text = if (menuItem.isInExchange)
                            stringResource(R.string.canteen_take_from_exchange)
                        else
                            stringResource(R.string.canteen_put_on_exchange)
                    )
                }

            if (menuItem.isEnabled)
                Button(onClick = onOrderClick) {
                    Text(
                        text = if (menuItem.isOrdered)
                            stringResource(R.string.canteen_cancel_order)
                        else
                            stringResource(R.string.canteen_order)
                    )
                }
        }
    ) {
        // To be added
        /*AsyncImage(
            modifier = Modifier
                .height(180.dp)
                .clip(RoundedCornerShape(28.dp)),
            model = "https://comfortablefood.com/wp-content/uploads/2022/05/beef-chop-suey-featured.jpg",
            contentDescription = null,
            contentScale = ContentScale.Crop
        )*/
    }
}

private data class MenuItemWithNumber(val menuItem: MenuItem, val number: Int)

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.")