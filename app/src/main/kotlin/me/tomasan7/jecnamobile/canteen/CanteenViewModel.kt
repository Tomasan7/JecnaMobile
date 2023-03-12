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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
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
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    var uiState by mutableStateOf(CanteenState())
        private set

    private var loadMenuJob: Job? = null

    init
    {
        viewModelScope.launch {
            if (uiState.menuPage != null)
                return@launch

            changeUiState(loading = true)

            if (jecnaClient.lastSuccessfulLoginAuth == null)
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.canteen_login_error)))
            else
                canteenClient.login(jecnaClient.lastSuccessfulLoginAuth!!)

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
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_order)))
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
                changeUiState(snackBarMessageEvent = triggered(appContext.getString(R.string.error_order)))
            }

            changeUiState(orderInProcess = false)
        }
    }

    fun requestImage(menuItem: MenuItem)
    {
        if (uiState.images.contains(menuItem) || menuItem.description == null)
            return

        viewModelScope.launch {
            try
            {
                val dishMatchResult = requestDishMatchResult(menuItem.description!!.rest)

                if (dishMatchResult.compareResult.matchPart.last <= 10)
                    return@launch

                changeUiState(images = uiState.images + (menuItem to dishMatchResult))
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    private suspend fun requestDishMatchResult(dishDescription: String): DishMatchResult
    {
        return httpClient.get("$CANTEEN_IMAGES_HOST/api/dishes") {
            parameter("description", dishDescription)
        }.body()
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
                changeUiState(
                    loading = false,
                    snackBarMessageEvent = triggered(appContext.getString(R.string.error_unsupported_menu))
                )
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                changeUiState(
                    loading = false,
                    snackBarMessageEvent = triggered(appContext.getString(R.string.error_load))
                )
            }
        }
    }

    fun reload() = loadMenu()

    fun onSnackBarMessageEventConsumed() = changeUiState(snackBarMessageEvent = consumed())

    fun changeUiState(
        loading: Boolean = uiState.loading,
        orderInProcess: Boolean = uiState.orderInProcess,
        menuPage: MenuPage? = uiState.menuPage,
        images: ImagesMap = uiState.images,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            menuPage = menuPage,
            images = images,
            orderInProcess = orderInProcess,
            snackBarMessageEvent = snackBarMessageEvent,
        )
    }

    companion object
    {
        const val CANTEEN_IMAGES_HOST = "http://192.168.1.10:80"
    }
}