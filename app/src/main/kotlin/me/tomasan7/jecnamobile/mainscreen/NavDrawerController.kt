package me.tomasan7.jecnamobile.mainscreen

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NavDrawerController @OptIn(ExperimentalMaterial3Api::class) constructor(
    private val drawerState: DrawerState,
    private val coroutineScope: CoroutineScope
) {
    @OptIn(ExperimentalMaterial3Api::class)
    fun close() = coroutineScope.launch {
        drawerState.close()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun open() = coroutineScope.launch {
        drawerState.open()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberNavDrawerController(
    drawerState: DrawerState,
    coroutineScope: CoroutineScope
): NavDrawerController {
    return remember { NavDrawerController(drawerState, coroutineScope) }
}