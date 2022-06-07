package me.tomasan7.jecnamobile

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme

class MainActivity : ComponentActivity()
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            JecnaMobileTheme {
                Column {
                    Text(intent.getStringExtra("username") as String)
                    Text((intent.getStringExtra("password") as String).map { '*' }.joinToString(""))
                    val context = LocalContext.current

                    Button(onClick = {
                        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            clear()
                            apply()
                        }
                    }, content = { Text("Clear saved auth") })
                }
            }
        }
    }
}