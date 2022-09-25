package me.tomasan7.jecnamobile.screen.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.web.ICanteenWebClient
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val jecnaWebClient: JecnaWebClient,
    private val iCanteenWebClient: ICanteenWebClient
) : ViewModel()
{
    fun logout()
    {
        runBlocking {
            launch {
                jecnaWebClient.logout()
            }
            launch {
                iCanteenWebClient.logout()
            }
        }

        authRepository.clear()
    }
}