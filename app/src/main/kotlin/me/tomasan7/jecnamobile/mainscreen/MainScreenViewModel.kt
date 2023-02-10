package me.tomasan7.jecnamobile.mainscreen

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnamobile.JecnaMobileApplication
import me.tomasan7.jecnamobile.login.AuthRepository
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val authRepository: AuthRepository,
    private val jecnaClient: JecnaClient,
    //private val canteenClient: CanteenClient
) : ViewModel()
{
    private val connectivityManager = getSystemService(appContext, ConnectivityManager::class.java) as ConnectivityManager
    private val loginNetworkCallback = LoginNetworkCallback()

    init
    {
        registerNetworkAvailabilityListener()
    }

    fun tryLogin()
    {
        val auth = jecnaClient.lastLoginAuth ?: authRepository.get() ?: TODO()  // Navigate back to login screen

        viewModelScope.launch {
            try
            {
                val loginResult = try
                {
                    Log.d("MainScreenViewModel", "Logging in...")
                    jecnaClient.login(auth)
                }
                catch (e: Exception) { false }

                if (loginResult == false)
                    // TODO Navigate back to login screen
                else
                    broadcastSuccessfulLogin()
            }
            catch (e: CancellationException)
            {
                throw e
            }
            catch (e: Exception) {}
        }
    }

    private fun broadcastSuccessfulLogin()
    {
        val intent = Intent(JecnaMobileApplication.SUCCESSFUL_LOGIN_ACTION)
        appContext.sendBroadcast(intent)
    }

    fun logout()
    {
        runBlocking {
            launch {
                jecnaClient.logout()
            }
            launch {
                //canteenClient.logout()
            }
        }

        authRepository.clear()
    }

    private fun registerNetworkAvailabilityListener()
    {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, loginNetworkCallback)
    }

    private fun unregisterNetworkAvailabilityListener()
    {
        connectivityManager.unregisterNetworkCallback(loginNetworkCallback)
    }

    override fun onCleared() = unregisterNetworkAvailabilityListener()

    inner class LoginNetworkCallback : ConnectivityManager.NetworkCallback()
    {
        override fun onAvailable(network: android.net.Network) = tryLogin()
    }
}