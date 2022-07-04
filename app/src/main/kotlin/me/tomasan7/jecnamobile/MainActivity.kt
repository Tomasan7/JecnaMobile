package me.tomasan7.jecnamobile

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.destinations.LoginScreenDestination
import me.tomasan7.jecnamobile.destinations.MainScreenDestination
import me.tomasan7.jecnamobile.destinations.NetworkErrorScreenDestination
import me.tomasan7.jecnamobile.screen.viewmodel.LoginScreenViewModel
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme
import java.net.InetAddress
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity()
{
    @Inject
    lateinit var jecnaClient: JecnaWebClient

    private val authPreferences
        get() = getSharedPreferences(LoginScreenViewModel.AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {

            val startRoute = remember {
                if (!isAuthSaved())
                    LoginScreenDestination
                else
                {
                    val loginResult = runBlocking {
                        try
                        {
                            if (jecnaClient.login(getSavedAuth()))
                            {
                                LoginResult.SUCCESS
                            }
                            else
                                LoginResult.AUTH_ERROR
                        }
                        catch (e: Exception)
                        {
                            LoginResult.NETWORK_ERROR
                        }
                    }

                    when (loginResult)
                    {
                        LoginResult.SUCCESS       -> MainScreenDestination
                        LoginResult.AUTH_ERROR    -> LoginScreenDestination
                        LoginResult.NETWORK_ERROR -> NetworkErrorScreenDestination
                    }
                }
            }

            JecnaMobileTheme {
                DestinationsNavHost(navGraph = NavGraphs.root,
                                    modifier = Modifier.fillMaxSize(),
                                    startRoute = startRoute)
            }
        }
    }

    private enum class LoginResult
    {
        SUCCESS,
        NETWORK_ERROR,
        AUTH_ERROR
    }

    private fun isAuthSaved() = authPreferences.contains(LoginScreenViewModel.PREFERENCES_USERNAME_KEY)

    private fun getSavedAuth() =
        Auth(authPreferences.getString(LoginScreenViewModel.PREFERENCES_USERNAME_KEY, null) as String,
             authPreferences.getString(LoginScreenViewModel.PREFERENCES_PASSWORD_KEY, null) as String)

    private fun isInternetAvailable(): Boolean
    {
        return try
        {
            val ipAddr = InetAddress.getByName(JecnaWebClient.ENDPOINT)
            //You can replace it with your name
            !ipAddr.equals("")
        }
        catch (e: Exception)
        {
            false
        }
    }
}