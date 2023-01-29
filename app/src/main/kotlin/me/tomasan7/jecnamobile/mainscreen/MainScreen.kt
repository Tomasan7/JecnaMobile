package me.tomasan7.jecnamobile.mainscreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@Destination
@Composable
fun MainScreen()
{
    Text("MainScreen placeholder...", color = MaterialTheme.colorScheme.onBackground)
}