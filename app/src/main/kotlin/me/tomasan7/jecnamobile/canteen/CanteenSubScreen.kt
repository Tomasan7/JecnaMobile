package me.tomasan7.jecnamobile.canteen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import de.palm.composestateevents.EventEffect
import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.mainscreen.NavDrawerController
import me.tomasan7.jecnamobile.mainscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.ui.ElevationLevel
import me.tomasan7.jecnamobile.ui.component.*
import me.tomasan7.jecnamobile.ui.theme.canteen_dish_description_difference
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_disabled
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_ordered
import me.tomasan7.jecnamobile.ui.theme.jm_canteen_ordered_disabled
import me.tomasan7.jecnamobile.util.getWeekDayName
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import me.tomasan7.jecnamobile.util.settingsAsState
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@SubScreensNavGraph
@Destination
@Composable
fun CanteenSubScreen(
    navDrawerController: NavDrawerController,
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
    val menuItemDialogState = rememberObjectDialogState<MenuItem>()
    val allergensDialogState = rememberObjectDialogState<DayMenu>()
    val helpDialogState = rememberObjectDialogState<Unit>()
    val pullRefreshState = rememberPullRefreshState(uiState.loading, viewModel::reload)
    val snackbarHostState = remember { SnackbarHostState() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { successful ->
        if (successful)
            viewModel.onImagePicked()
    }

    EventEffect(
        event = uiState.takeImageEvent,
        onConsumed = viewModel::onTakeImageEventConsumed
    ) {
        launcher.launch(it)
    }

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
                navDrawerController = navDrawerController,
                actions = {
                    if (uiState.menuPage != null)
                        Credit(uiState.menuPage.credit)
                    IconButton(onClick = { helpDialogState.show(Unit) }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null
                        )
                    }
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
                                onMenuItemLongClick = { viewModel.orderMenuItem(it) },
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

            val settings by settingsAsState()

            ObjectDialog(
                state = menuItemDialogState,
                /* https://stackoverflow.com/questions/68818202/animatedvisibility-doesnt-expand-height-in-dialog-in-jetpack-compose/68818540#68818540 */
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { menuItemDialogState.hide() },
                content = { menuItem ->
                    val imageState = when
                    {
                        !uiState.images.containsKey(menuItem) -> ImageState.Loading
                        uiState.images[menuItem] == null      -> ImageState.NotFound
                        else                                  -> ImageState.Found(uiState.images[menuItem]!!)
                    }

                    MenuItemDialogContent(
                        menuItem = menuItem,
                        imageState = imageState,
                        onOpen = {
                            viewModel.requestImage(menuItem)
                        },
                        onOrderClick = {
                            viewModel.orderMenuItem(menuItem)
                            menuItemDialogState.hide()
                        },
                        onPutOnExchangeClick = {
                            viewModel.putMenuItemOnExchange(menuItem)
                            menuItemDialogState.hide()
                        },
                        onCloseClick = {
                            menuItemDialogState.hide()
                        },
                        onUploadClick = {
                            viewModel.takeImage(menuItem)
                        },
                        isUploader = uiState.isUploader
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

            ObjectDialog(
                state = helpDialogState,
                /* https://stackoverflow.com/questions/68818202/animatedvisibility-doesnt-expand-height-in-dialog-in-jetpack-compose/68818540#68818540 */
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { helpDialogState.hide() },
                content = {
                    HelpDialogContent(
                        onCloseCLick = { helpDialogState.hide() }
                    )
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
    onMenuItemClick: (MenuItem) -> Unit = {},
    onMenuItemLongClick: (MenuItem) -> Unit = {}
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

            dayMenu.items.forEach { menuItem ->
                key(menuItem) {
                    MenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        menuItem = menuItem,
                        onClick = { onMenuItemClick(menuItem) },
                        onLongClick = { onMenuItemLongClick(menuItem) }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MenuItem(
    menuItem: MenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
)
{
    val color = when
    {
        menuItem.isOrdered && !menuItem.isEnabled -> jm_canteen_ordered_disabled
        menuItem.isOrdered                        -> jm_canteen_ordered
        !menuItem.isEnabled                       -> jm_canteen_disabled
        else                                      -> MaterialTheme.colorScheme.surface
    }

    val lunchString = stringResource(R.string.canteen_lunch, menuItem.number)

    val text = remember(menuItem.description?.rest) {
        menuItem.description?.rest?.replaceFirstChar { it.uppercase() }?.replace(" , ", ", ")
            ?: lunchString
    }

    ElevatedTextRectangle(
        text = { Text(text, modifier = Modifier.weight(1f)) },
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        color = color,
        trailingIcon = if (menuItem.isInExchange)
        {
            {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    tint = Color.Gray.copy(alpha = .5f),
                    contentDescription = null
                )
            }
        }
        else
            null
    )
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
            dayMenu.items.forEach { menuItem ->
                AllergensForMenuItem(menuItem)
            }
        }
    }
}

@Composable
private fun AllergensForMenuItem(menuItem: MenuItem)
{
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.canteen_lunch, menuItem.number))

        ElevatedTextRectangle(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = menuItem.allergens?.joinToString() ?: "",
                textAlign = TextAlign.Center
            )
        }
    }
}

private interface ImageState
{
    object Loading : ImageState
    data class Found(val result: DishMatchResult) : ImageState
    object NotFound : ImageState
}

@Composable
private fun MenuItemDialogContent(
    menuItem: MenuItem,
    imageState: ImageState,
    onOpen: () -> Unit = {},
    onOrderClick: () -> Unit = {},
    onPutOnExchangeClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    isUploader: Boolean = false,
    onUploadClick: () -> Unit = {}
)
{
    DisposableEffect(Unit) {
        onOpen()
        onDispose {}
    }

    DialogContainer(
        modifier = Modifier.padding(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.canteen_lunch, menuItem.number))

                if (isUploader)
                    IconButton(onClick = onUploadClick) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null
                        )
                    }
            }
        },
        buttons = {
            TextButton(onClick = onCloseClick) {
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
        val settings by settingsAsState()

        when (imageState)
        {
            is ImageState.Loading -> LinearProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 20.dp, horizontal = 10.dp)
                    .fillMaxWidth(),
            )

            is ImageState.Found   ->
            {
                val dishMatchResult = imageState.result
                var showPicture by remember {
                    mutableStateOf(dishMatchResult.compareResult.score >= settings.canteenImageTolerance)
                }
                if (showPicture)
                    DishPicture(dishMatchResult)
                else
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPicture = true }
                            .padding(vertical = 20.dp, horizontal = 10.dp)
                            .fillMaxWidth(),
                        text = stringResource(
                            R.string.canteen_image_innacurate,
                            (dishMatchResult.compareResult.score * 100.0).roundToInt()
                        ),
                        textAlign = TextAlign.Center
                    )
            }
        }

        val menuItemDescription = remember(menuItem) {
            menuItem.description?.rest?.replaceFirstChar { it.uppercase() }
                ?.replace(" , ", ", ")
        }

        ElevatedTextRectangle(
            modifier = Modifier.fillMaxWidth(),
            text = menuItemDescription ?: stringResource(R.string.canteen_lunch, menuItem.number)
        )
    }
}

@Composable
private fun DishPicture(
    dishMatchResult: DishMatchResult
)
{
    var pictureNameShown by rememberMutableStateOf(false)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { pictureNameShown = !pictureNameShown },
            model = "${CanteenViewModel.CANTEEN_IMAGES_HOST}/api/images/${dishMatchResult.dish.imageId}",
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        AnimatedVisibility(pictureNameShown) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val imageDishDescription = remember(dishMatchResult) {
                    dishDescriptionString(dishMatchResult = dishMatchResult)
                }
                ElevatedTextRectangle(
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(imageDishDescription) }
                )
                VerticalDivider(
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .padding(vertical = 10.dp)
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
                )
            }
        }
    }
}

private fun dishDescriptionString(
    dishMatchResult: DishMatchResult
): AnnotatedString
{
    val regex = Regex(" , ")
    val matchCount = regex
        .findAll(dishMatchResult.dish.description)
        .count { it.range.first < dishMatchResult.compareResult.matchPart.last }

    return buildAnnotatedString {
        val modified = dishMatchResult.dish.description
            .replace(regex, ", ")
            .replaceFirstChar { it.uppercase() }
        append(modified)
        addStyle(
            style = SpanStyle(
                color = canteen_dish_description_difference
            ),
            start = dishMatchResult.compareResult.matchPart.last + 1 - matchCount,
            end = modified.length
        )
    }
}

@Composable
private fun HelpDialogContent(
    onCloseCLick: () -> Unit = {}
) =
    DialogContainer(
        modifier = Modifier.padding(24.dp),
        title = {
            Text(stringResource(R.string.canteen_help_title))
        },
        buttons = {
            TextButton(onClick = onCloseCLick) {
                Text(stringResource(R.string.close))
            }
        }
    ) {
        val content = stringArrayResource(R.array.canteen_help_points)

        if (content.size % 2 != 0)
            throw IllegalStateException("Each help point must have a title.")

        for (i in content.indices step 2)
        {
            Text(
                text = content[i],
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = content[i + 1],
                style = MaterialTheme.typography.bodyMedium
            )
            if (i != content.size - 2)
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
                )
        }
    }

@Composable
private fun ElevatedTextRectangle(
    modifier: Modifier = Modifier,
    elevation: Dp = ElevationLevel.level5,
    color: Color = MaterialTheme.colorScheme.surface,
    trailingIcon: @Composable (RowScope.() -> Unit)? = null,
    text: @Composable RowScope.() -> Unit
)
{
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = elevation,
        color = color,
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            text()
            if (trailingIcon != null)
            {
                HorizontalSpacer(10.dp)
                trailingIcon()
            }
        }
    }
}

@Composable
private fun ElevatedTextRectangle(
    modifier: Modifier = Modifier,
    elevation: Dp = ElevationLevel.level5,
    color: Color = MaterialTheme.colorScheme.surface,
    trailingIcon: ImageVector? = null,
    text: String
) = ElevatedTextRectangle(
    modifier = modifier,
    elevation = elevation,
    color = color,
    trailingIcon = trailingIcon?.let { { Icon(it, null) } },
    text = { Text(text, modifier = Modifier.weight(1f)) }
)

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.")