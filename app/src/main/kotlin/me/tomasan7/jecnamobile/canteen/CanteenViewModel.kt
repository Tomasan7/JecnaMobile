package me.tomasan7.jecnamobile.canteen

import android.content.Context
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.CanteenClient
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.util.settingsDataStore
import java.time.DayOfWeek
import java.time.LocalDate
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
            changeUiState(loading = true)

            if (jecnaClient.lastSuccessfulLoginAuth != null)
                try
                {
                    canteenClient.login(jecnaClient.lastSuccessfulLoginAuth!!)
                }
                catch (e: Exception)
                {
                    changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.canteen_login_error)))
                    e.printStackTrace()
                }
            else
            {
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.canteen_login_error)))
            }

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

    fun orderMenuItem(menuItem: MenuItem, dayMenuDate: LocalDate)
    {
        if (uiState.orderInProcess)
            return

        changeUiState(orderInProcess = true)

        viewModelScope.launch {
            try
            {
                canteenClient.order(menuItem)
                val newDayMenu = canteenClient.getDayMenu(dayMenuDate)
                updateMenu(newDayMenu)
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
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_order)))
            }

            changeUiState(orderInProcess = false)
        }
    }

    fun putMenuItemOnExchange(menuItem: MenuItem, dayMenuDate: LocalDate)
    {
        if (uiState.orderInProcess)
            return

        if (!menuItem.isOrdered)
            return

        if (menuItem.isEnabled)
            return

        changeUiState(orderInProcess = true)

        viewModelScope.launch {
            try
            {
                canteenClient.putOnExchange(menuItem)
                val newDayMenu = canteenClient.getDayMenu(dayMenuDate)
                updateMenu(newDayMenu)
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
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_order)))
            }

            changeUiState(orderInProcess = false)
        }
    }

    fun loadMenu()
    {
        changeUiState(loading = true)

        loadMenuJob?.cancel()

        changeUiState(menu = emptyList())

        loadMenuJob = canteenClient.getMenuAsync(getDays())
            .onEach { addDayMenu(it) }
            .onCompletion { changeUiState(loading = false) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            try
            {
                val credit = canteenClient.getCredit()
                changeUiState(credit = credit)
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_unsupported_menu)))
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_load)))
            }
        }
    }

    private fun getDays(): List<LocalDate>
    {
        val result = mutableListOf<LocalDate>()
        var cursor = LocalDate.now()
        while(cursor.dayOfWeek != DayOfWeek.SATURDAY)
        {
            result.add(cursor)
            cursor = cursor.plusDays(1)
        }

        return result
    }

    fun reload() = loadMenu()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    fun onHelpDialogShownAutomatically() = viewModelScope.launch {
        appContext.settingsDataStore.updateData {
            it.copy(
                canteenHelpSeen = true
            )
        }
    }

    private fun updateMenu(newDayMenu: DayMenu)
    {
        val menu = uiState.menu.map { if (it.day == newDayMenu.day) newDayMenu else it }
        changeUiState(menu = menu)
    }

    private fun addDayMenu(newDayMenu: DayMenu)
    {
        val menu = uiState.menu.toMutableList()
        menu.add(newDayMenu)
        changeUiState(menu = menu)
    }

    private fun changeUiState(
        loading: Boolean = uiState.loading,
        orderInProcess: Boolean = uiState.orderInProcess,
        menu: List<DayMenu> = uiState.menu,
        credit: Float? = uiState.credit,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            menu = menu,
            credit = credit,
            orderInProcess = orderInProcess,
            snackBarMessageEvent = snackBarMessageEvent,
        )
    }
}