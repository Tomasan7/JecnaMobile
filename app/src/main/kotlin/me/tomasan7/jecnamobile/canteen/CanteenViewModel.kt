package me.tomasan7.jecnamobile.canteen

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.CanteenClient
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.canteen.MenuPage
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnamobile.R
import javax.inject.Inject

@HiltViewModel
class CanteenViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val jecnaClient: JecnaClient,
    private val canteenClient: CanteenClient,
) : ViewModel()
{
    var uiState by mutableStateOf(CanteenState())
        private set

    private var loadMenuJob: Job? = null

    init
    {
        viewModelScope.launch {
            if (uiState.menuPage != null)
                return@launch

            changeUiState(loading = true)

            if (jecnaClient.lastLoginAuth == null)
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.canteen_login_error)))
            else
                canteenClient.login(jecnaClient.lastLoginAuth!!)

            loadMenu()
        }
    }

    fun enteredComposition()
    {

    }

    fun leftComposition()
    {
        loadMenuJob?.cancel()
    }

    fun orderMenuItem(menuItem: MenuItem)
    {
        if (uiState.orderInProcess)
            return

        changeUiState(orderInProcess = true)

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

            changeUiState(orderInProcess = false)
        }
    }

    fun putMenuItemOnExchange(menuItem: MenuItem)
    {
        if (uiState.orderInProcess)
            return

        changeUiState(orderInProcess = true)

        viewModelScope.launch {
            try
            {
                canteenClient.putOnExchange(menuItem, uiState.menuPage!!)
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

            changeUiState(orderInProcess = false)
        }
    }

    fun loadMenu()
    {
        changeUiState(loading = true)

        loadMenuJob?.cancel()

        loadMenuJob = viewModelScope.launch {
            try
            {
                val menuPage = canteenClient.getMenuPage()
                changeUiState(loading = false, menuPage = menuPage)
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_unsupported_menu), Toast.LENGTH_LONG).show()
                changeUiState(loading = false)
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
                changeUiState(loading = false)
            }
        }
    }

    fun reload() = loadMenu()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    fun changeUiState(
        loading: Boolean = uiState.loading,
        orderInProcess: Boolean = uiState.orderInProcess,
        menuPage: MenuPage? = uiState.menuPage,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            menuPage = menuPage,
            orderInProcess = orderInProcess,
            snackBarMessageEvent = snackBarMessageEvent,
        )
    }
}