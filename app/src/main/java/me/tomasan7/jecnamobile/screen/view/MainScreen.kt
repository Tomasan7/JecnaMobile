package me.tomasan7.jecnamobile.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.NavGraphs
import me.tomasan7.jecnamobile.subscreen.view.GradesSubScreen

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun MainScreen(
    jecnaWebClient: JecnaWebClient?
)
{
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    data class DrawerItem(val icon: ImageVector, val label: String)

    // icons to mimic drawer destinations
    val items = listOf(
        DrawerItem(Icons.Default.Star, "Známky"),
        DrawerItem(Icons.Default.DateRange, "Příchody")
    )

    val selectedItem = remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerState = drawerState,
        drawerContent = {
            Row(
                modifier = Modifier.height(56.dp).padding(start = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ječná Mobile",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items.forEach { item ->
                NavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    icon = { Icon(item.icon, null) },
                    label = { Text(item.label) },
                    selected = item == selectedItem.value,
                    onClick = {
                        selectedItem.value = item
                        scope.launch { drawerState.close() }
                    }
                )
            }
        },
        content = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text(selectedItem.value.label) },
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }
    )
}