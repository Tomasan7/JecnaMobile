package me.tomasan7.jecnamobile

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import me.tomasan7.jecnamobile.destinations.LoginScreenDestination
import me.tomasan7.jecnamobile.destinations.MainScreenDestination
import me.tomasan7.jecnamobile.screen.viewmodel.LoginScreenViewModel
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme

class MainActivity : ComponentActivity()
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val authSaved =
            getSharedPreferences(LoginScreenViewModel.AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).contains(
                LoginScreenViewModel.PREFERENCES_USERNAME_KEY)

        setContent {
            JecnaMobileTheme {
                DestinationsNavHost(navGraph = NavGraphs.root,
                                    modifier = Modifier.fillMaxSize(),
                                    startRoute = if (authSaved) MainScreenDestination else LoginScreenDestination)
            }
        }
    }
}