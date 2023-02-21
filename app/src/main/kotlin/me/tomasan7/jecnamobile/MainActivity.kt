package me.tomasan7.jecnamobile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import me.tomasan7.jecnamobile.destinations.LoginScreenDestination
import me.tomasan7.jecnamobile.destinations.MainScreenDestination
import me.tomasan7.jecnamobile.login.AuthRepository
import me.tomasan7.jecnamobile.settings.isAppInDarkTheme
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme
import me.tomasan7.jecnamobile.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity()
{
    @Inject
    lateinit var authRepository: AuthRepository

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val startDestination = if (authRepository.exists())
            MainScreenDestination
        else
            LoginScreenDestination

        setContent {
            val settings by settingsAsStateAwaitFirst()
            val isAppInDarkTheme = isAppInDarkTheme(settings)

            JecnaMobileTheme(isAppInDarkTheme) {
                val backgroundColor = MaterialTheme.colorScheme.background

                SystemBarColors(isAppInDarkTheme, backgroundColor)

                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    startRoute = startDestination,
                    modifier = Modifier.fillMaxSize().background(backgroundColor)
                )
            }
        }
    }
}

@Composable
fun SystemBarColors(isAppInDarkTheme: Boolean, color: Color = MaterialTheme.colorScheme.background)
{
    // https://google.github.io/accompanist/systemuicontroller/
    val systemUiController = rememberSystemUiController()

    DisposableEffect(isAppInDarkTheme) {
        systemUiController.setSystemBarsColor(
            color = color,
            isNavigationBarContrastEnforced = false,
            darkIcons = !isAppInDarkTheme
        )
        // setStatusBarColor() and setNavigationBarColor() also exist
        onDispose {}
    }
}