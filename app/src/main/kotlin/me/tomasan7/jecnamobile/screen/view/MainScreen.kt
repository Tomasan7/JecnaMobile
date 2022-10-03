package me.tomasan7.jecnamobile.screen.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.findDestination
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.NavGraphs
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.*
import me.tomasan7.jecnamobile.screen.viewmodel.MainScreenViewModel
import me.tomasan7.jecnamobile.util.rememberMutableStateOf
import me.tomasan7.jecnamobile.util.awaitSettings


@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@com.ramcosta.composedestinations.annotation.Destination
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = hiltViewModel()
)
{
    val settings = awaitSettings()
    val openSubScreen = remember { NavGraphs.subScreens.findDestination(settings.openSubScreenRoute)!! }

    data class DrawerItem(val icon: ImageVector, val label: String, val destination: Destination)
    data class DrawerLinkItem(val icon: ImageVector, val label: String, val link: String)

    val destinationItems = listOf(
        DrawerItem(Icons.Default.Newspaper, stringResource(R.string.sidebar_news), NewsSubScreenDestination),
        DrawerItem(Icons.Default.Star, stringResource(R.string.sidebar_grades), GradesSubScreenDestination),
        DrawerItem(Icons.Default.TableChart, stringResource(R.string.sidebar_timetable), TimetableSubScreenDestination),
        DrawerItem(Icons.Default.RestaurantMenu, stringResource(R.string.sidebar_canteen), CanteenSubScreenDestination),
        DrawerItem(Icons.Default.DateRange, stringResource(R.string.sidebar_attendances), AttendancesSubScreenDestination)
    )
    val linkItems = listOf(
        DrawerLinkItem(Icons.Default.TableChart, stringResource(R.string.sidebar_link_substitution_timetable), "${JecnaWebClient.ENDPOINT}/suplovani")
    )
    val settingsItem = DrawerItem(Icons.Default.Settings, stringResource(R.string.sidebar_settings), SettingsSubScreenDestination)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contentNavController = rememberNavController()

    var selectedItem by rememberMutableStateOf(destinationItems.find { it.destination == openSubScreen }!!)

    contentNavController.addOnDestinationChangedListener { _, destination,_ ->
        val newSelectedItem = destinationItems.find { it.destination.route == destination.route }
        if (newSelectedItem != null)
            selectedItem = newSelectedItem
    }

    val context = LocalContext.current
    ModalNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(start = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    destinationItems.forEach { item ->
                        val selected by remember { derivedStateOf { item == selectedItem } }
                        SidebarItem(
                            label = item.label,
                            icon = item.icon,
                            selected = selected,
                            onClick = onClick@ {
                                scope.launch { drawerState.close() }
                                if (selected)
                                    return@onClick

                                contentNavController.navigate(item.destination.route)
                                selectedItem = item
                            }
                        )
                    }
                    Divider(Modifier.fillMaxWidth().padding(vertical = 10.dp))
                    linkItems.forEach { item ->
                        SidebarItem(
                            label = item.label,
                            icon = item.icon,
                            selected = false,
                            badgeIcon = Icons.Default.OpenInBrowser,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(item.link)
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val selected by remember { derivedStateOf { settingsItem == selectedItem } }
                    SidebarItem(
                        icon = settingsItem.icon,
                        label = settingsItem.label,
                        selected = selected,
                        onClick = onClick@{
                            scope.launch { drawerState.close() }
                            if (selected)
                                return@onClick

                            contentNavController.navigate(settingsItem.destination.route)
                            selectedItem = settingsItem
                        }
                    )
                    NavigationDrawerItem(
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        icon = { Icon(Icons.Default.Logout, null) },
                        label = { Text(stringResource(R.string.sidebar_logout)) },
                        selected = false,
                        onClick = onClick@{
                            navController.navigate(LoginScreenDestination) {
                                navController.currentDestination?.id?.let {
                                    popUpTo(it) {
                                        inclusive = true
                                    }
                                }
                            }
                            viewModel.logout()
                        }
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(selectedItem.label) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                    )
                }
            ) { paddingValues ->
                val startRoute = remember { NavGraphs.subScreens.findDestination(settings.openSubScreenRoute)!! }

                DestinationsNavHost(
                    navGraph = NavGraphs.subScreens,
                    startRoute = startRoute,
                    navController = contentNavController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeIcon: ImageVector? = null,
    onClick: () -> Unit = {}
)
{
    NavigationDrawerItem(
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        icon = { Icon(icon, label) },
        label = { Text(label) },
        badge = badgeIcon?.let { { Icon(it, label) } },
        selected = selected,
        onClick = onClick
    )
}