package me.tomasan7.jecnamobile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme

class MainActivity : ComponentActivity()
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            JecnaMobileTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen()
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
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = if (drawerState.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<",
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { scope.launch { drawerState.open() } }) {
                            Text("Click to open")
                        }
                    }
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Preview
    @Composable
    fun MainScreenPreview()
    {
        JecnaMobileTheme {
            MainScreen()
        }
    }
}