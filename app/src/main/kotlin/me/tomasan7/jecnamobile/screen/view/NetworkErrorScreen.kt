package me.tomasan7.jecnamobile.screen.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RequiresApi(Build.VERSION_CODES.S)
@RootNavGraph
@Destination
@Composable
fun NetworkErrorScreen()
{
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Network error", color = MaterialTheme.colorScheme.onBackground)
    }
}