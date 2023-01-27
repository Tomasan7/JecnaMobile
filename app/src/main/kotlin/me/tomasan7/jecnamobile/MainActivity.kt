package me.tomasan7.jecnamobile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import me.tomasan7.jecnamobile.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity()
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            Text("Hello World!", color = Color.White)
        }
    }
}