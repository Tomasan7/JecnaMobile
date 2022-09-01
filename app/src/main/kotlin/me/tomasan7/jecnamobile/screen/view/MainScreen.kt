package me.tomasan7.jecnamobile.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.NavGraphs
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.AttendancesSubScreenDestination
import me.tomasan7.jecnamobile.destinations.Destination
import me.tomasan7.jecnamobile.destinations.GradesSubScreenDestination
import me.tomasan7.jecnamobile.destinations.TimetableSubScreenDestination
import me.tomasan7.jecnamobile.util.rememberMutableStateOf

data class DrawerItem(val icon: ImageVector, val label: String, val destination: Destination)

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@com.ramcosta.composedestinations.annotation.Destination
@Composable
fun MainScreen()
{
    val destinationItems = listOf(
            DrawerItem(Icons.Default.Star, stringResource(R.string.sidebar_grades), GradesSubScreenDestination),
            DrawerItem(Icons.Default.DateRange, stringResource(R.string.sidebar_attendances), AttendancesSubScreenDestination),
            DrawerItem(Icons.Default.TableChart, stringResource(R.string.sidebar_timetable), TimetableSubScreenDestination)
        )

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contentNavController = rememberNavController()

    var selectedItem by rememberMutableStateOf(destinationItems[0])

    contentNavController.addOnDestinationChangedListener { _, destination,_ ->
        val newSelectedItem = destinationItems.find { it.destination.route == destination.route }
        if (newSelectedItem != null)
            selectedItem = newSelectedItem
    }

    ModalNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier.height(56.dp).padding(start = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                destinationItems.forEach { item ->
                    NavigationDrawerItem(
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) },
                        selected = item == selectedItem,
                        onClick = onClick@ {
                            scope.launch { drawerState.close() }
                            if (selectedItem == item)
                                return@onClick

                            contentNavController.navigate(item.destination.route)
                            selectedItem = item
                        }
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text(selectedItem.label) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Localized description"
                                )
                            }
                        },
                    )
                }
            ) { paddingValues ->
                DestinationsNavHost(
                    navGraph = NavGraphs.subScreens,
                    navController = contentNavController,
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }
    )
}