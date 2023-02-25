package me.tomasan7.jecnamobile.mainscreen

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.findDestination
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.jecna.JecnaWebClient
import me.tomasan7.jecnamobile.NavGraphs
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.attendances.AttendancesSubScreen
import me.tomasan7.jecnamobile.canteen.CanteenSubScreen
import me.tomasan7.jecnamobile.destinations.*
import me.tomasan7.jecnamobile.grades.GradesSubScreen
import me.tomasan7.jecnamobile.news.NewsSubScreen
import me.tomasan7.jecnamobile.teachers.TeachersSubScreen
import me.tomasan7.jecnamobile.timetable.TimetableSubScreen
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import me.tomasan7.jecnamobile.util.settingsAsStateAwaitFirst

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph
@Destination
@Composable
fun MainScreen(
    navigator: DestinationsNavigator,
    viewModel: MainScreenViewModel = hiltViewModel()
)
{
    val settings by settingsAsStateAwaitFirst()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val destinationItems = SideBarDestination.values()
    val linkItems = SideBarLink.values()
    var selectedItem by rememberMutableStateOf(SideBarDestination.Timetable)
    val subScreensNavController = rememberNavController()
    val startRoute = remember { NavGraphs.subScreens.findDestination(settings.openSubScreenRoute)!! }
    val navDrawerController = rememberNavDrawerController(drawerState, scope)

    LaunchedEffect(subScreensNavController) {
        subScreensNavController.addOnDestinationChangedListener { _, destination, _ ->
            val newSelectedItem = destinationItems.find { it.destination.route == destination.route }
            if (newSelectedItem != null)
                selectedItem = newSelectedItem
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(all = 28.dp)
                )

                destinationItems.forEach { item ->
                    val selected = item === selectedItem
                    DestinationItem(
                        item = item,
                        selected = selected,
                        onClick = onClick@{
                            scope.launch { drawerState.close() }
                            if (selected)
                                return@onClick

                            subScreensNavController.navigate(item.destination.route)
                            selectedItem = item
                        }
                    )
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 28.dp)
                )

                linkItems.forEach { LinkItem(it) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    SidebarButtonsRow(
                        onSettingsClick = {
                            scope.launch { drawerState.close() }
                            subScreensNavController.navigate(SettingsScreenDestination)
                        },
                        onLogoutClick = {
                            viewModel.logout()
                            navigator.navigate(LoginScreenDestination)
                        }
                    )
                }
            }
        },
        content = {
            DestinationsNavHost(
                navGraph = NavGraphs.subScreens,
                startRoute = startRoute,
                navController = subScreensNavController,
                modifier = Modifier.fillMaxSize(),
                dependenciesContainerBuilder = {
                    dependency(NavGraphs.subScreens) { navDrawerController }
                }
            )
        }
    )
}

@Composable
private fun SidebarButtonsRow(
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Outlined.Settings, contentDescription = null)
        }

        IconButton(onClick = onLogoutClick) {
            Icon(Icons.Outlined.ExitToApp, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationItem(
    item: SideBarDestination,
    selected: Boolean,
    onClick: () -> Unit
)
{
    NavigationDrawerItem(
        icon = { Icon(if (selected) item.iconSelected else item.icon, contentDescription = null) },
        label = { Text(stringResource(item.label)) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItem(item: SideBarLink)
{
    val context = LocalContext.current
    val label = stringResource(item.label)

    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(item.link)
            context.startActivity(intent)
        },
        badge = { Icon(Icons.Outlined.OpenInBrowser, label) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

enum class SideBarDestination(
    val destination: DirectionDestinationSpec,
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon
)
{
    News(NewsSubScreenDestination, R.string.sidebar_news, Icons.Outlined.Newspaper, Icons.Filled.Newspaper),
    Grades(GradesSubScreenDestination, R.string.sidebar_grades, Icons.Outlined.Grade, Icons.Filled.Grade),
    Timetable(TimetableSubScreenDestination, R.string.sidebar_timetable, Icons.Outlined.TableChart, Icons.Filled.TableChart),
    Canteen(CanteenSubScreenDestination, R.string.sidebar_canteen, Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
    Attendances(AttendancesSubScreenDestination, R.string.sidebar_attendances, Icons.Outlined.DateRange, Icons.Filled.DateRange),
    Teachers(TeachersSubScreenDestination, R.string.sidebar_teachers, Icons.Outlined.People, Icons.Filled.People)
}

enum class SideBarLink(
    val link: String,
    @StringRes
    val label: Int,
    val icon: ImageVector
)
{
    SubstitutionTimetable(
        JecnaWebClient.getUrlForPath("/suplovani"), R.string.sidebar_link_substitution_timetable,
        Icons.Outlined.TableChart
    )
}