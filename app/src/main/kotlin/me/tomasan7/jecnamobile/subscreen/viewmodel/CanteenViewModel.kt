package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.repository.CanteenClient
import me.tomasan7.jecnaapi.web.ICanteenWebClient
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.repository.AuthRepository
import me.tomasan7.jecnamobile.subscreen.state.CanteenState
import javax.inject.Inject

@HiltViewModel
class CanteenViewModel @Inject constructor(
    private val iCanteenWebClient: ICanteenWebClient,
    private val canteenClient: CanteenClient,
    private val authRepository: AuthRepository,
    @ApplicationContext
    private val appContext: Context
) : ViewModel()
{
    var uiState by mutableStateOf(CanteenState())
        private set

    private var loadMenuJob: Job? = null

    init
    {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true)
            iCanteenWebClient.login(authRepository.get()!!)
            loadMenu()
        }
    }

    fun orderMenuItem(menuItem: MenuItem)
    {
        if (uiState.orderInProcess)
            return

        uiState = uiState.copy(orderInProcess = true)

        viewModelScope.launch {
            try
            {
                canteenClient.order(menuItem, uiState.menuPage!!)
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_order), Toast.LENGTH_LONG).show()
            }

            uiState = uiState.copy(orderInProcess = false)
        }
    }

    fun loadMenu()
    {
        uiState = uiState.copy(loading = true)

        loadMenuJob?.cancel()

        loadMenuJob = viewModelScope.launch {
            try
            {
                val menuPage = canteenClient.getMenuPage()
                uiState = uiState.copy(loading = false, menuPage = menuPage)
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_unsupported_menu), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_load), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
        }
    }
}