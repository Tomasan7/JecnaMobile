package me.tomasan7.jecnamobile.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenTopAppBar(
    title: String,
    onHamburgerClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
)
{
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onHamburgerClick) {
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
    onHamburgerClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
): Unit = SubScreenTopAppBar(stringResource(title), onHamburgerClick, actions)