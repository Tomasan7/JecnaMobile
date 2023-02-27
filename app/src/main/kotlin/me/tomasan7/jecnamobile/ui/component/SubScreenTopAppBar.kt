package me.tomasan7.jecnamobile.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.tomasan7.jecnamobile.mainscreen.NavDrawerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenTopAppBar(
    title: String,
    navDrawerController: NavDrawerController,
    actions: @Composable RowScope.() -> Unit = {}
)
{
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { navDrawerController.open() }) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        },
        actions = actions
    )
}

@Composable
fun SubScreenTopAppBar(
    @StringRes
    title: Int,
    navDrawerController: NavDrawerController,
    actions: @Composable RowScope.() -> Unit = {}
): Unit = SubScreenTopAppBar(stringResource(title), navDrawerController, actions)