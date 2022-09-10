package me.tomasan7.jecnamobile.subscreen.view

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.CanteenViewModel

@SubScreensNavGraph
@Destination
@Composable
fun CanteenSubScreen(
    viewModel: CanteenViewModel
)
{
    val uiState = viewModel.uiState

    
}