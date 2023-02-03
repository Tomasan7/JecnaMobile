package me.tomasan7.jecnamobile.mainscreen

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.NavGraphs
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.GradesSubScreenDestination
import me.tomasan7.jecnamobile.grades.GradesSubScreen
import me.tomasan7.jecnamobile.util.rememberMutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph
@Destination
@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel())
{
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val destinationItems = SideBarDestination.values()
    val linkItems = SideBarLink.values()
    var selectedItem by rememberMutableStateOf(destinationItems[0])
    val subScreensNavController = rememberNavController()

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
            }
        },
        content = {
            DestinationsNavHost(
                navGraph = NavGraphs.subScreens,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(GradesSubScreenDestination) {
                    GradesSubScreen(onHamburgerClick = { scope.launch { drawerState.open() } })
                }
            }
        }
    )
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
        icon = { Icon(item.icon, contentDescription = null) },
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
        badge = { Icon(Icons.Default.OpenInBrowser, label) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

enum class SideBarDestination(
    val destination: DirectionDestinationSpec,
    @StringRes
    val label: Int,
    val icon: ImageVector
)
{
    Grades(GradesSubScreenDestination, R.string.sidebar_grades, Icons.Default.Star)
}

enum class SideBarLink(
    val link: String,
    @StringRes
    val label: Int,
    val icon: ImageVector
)
{
    SubstitutionTimetable(
        "${JecnaWebClient.ENDPOINT}/suplovani", R.string.sidebar_link_substitution_timetable, Icons.Default.TableChart
    )
}