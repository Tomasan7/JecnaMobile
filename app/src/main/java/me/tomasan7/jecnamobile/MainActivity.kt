package me.tomasan7.jecnamobile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme

class MainActivity : ComponentActivity()
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            JecnaMobileTheme {
                Text("Hello World")
            }
        }
    }
}