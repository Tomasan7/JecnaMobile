package me.tomasan7.jecnamobile.canteen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
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
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tomasan7.jecnaapi.CanteenClient
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.canteen.MenuPage
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.login.CanteenServerPasswordRepository
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CanteenViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val canteenServerPasswordRepository: CanteenServerPasswordRepository,
    private val jecnaClient: JecnaClient,
    private val canteenClient: CanteenClient,
) : ViewModel()
{
    private var uploaderPassword: String? = null
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    var uiState by mutableStateOf(CanteenState())
        private set

    private var loadMenuJob: Job? = null

    private var latestTmpUri: Uri? = null
    private var currentlyTakingImageFor: MenuItem? = null

    init
    {
        viewModelScope.launch {
            if (uiState.menuPage != null)
                return@launch

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

            viewModelScope.launch {
                if (canteenServerPasswordRepository.exists())
                {
                    uploaderPassword = canteenServerPasswordRepository.get()
                    changeUiState(isUploader = true)
                }
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

    fun takeImage(menuItem: MenuItem)
    {
        currentlyTakingImageFor = menuItem
        latestTmpUri = getTmpFileUri()
        changeUiState(takeImageEvent = triggered(latestTmpUri!!))
    }

    fun onImagePicked()
    {
        val menuItem = currentlyTakingImageFor ?: return
        val dayMenu = uiState.menuPage?.menu?.dayMenus?.find { it.contains(menuItem) } ?: return

        if (menuItem.description == null)
            return

        val imageInputStream = latestTmpUri?.let { appContext.contentResolver.openInputStream(it) } ?: return
        val fileDescriptor = latestTmpUri?.let { appContext.contentResolver.openFileDescriptor(it, "r") } ?: return
        val imageChannel = imageInputStream.toByteReadChannel()

        viewModelScope.launch {
            httpClient.submitFormWithBinaryData(
                url = "$CANTEEN_IMAGES_HOST/api/upload",
                formData = formData {
                    append("password", uploaderPassword!!)
                    append("serveDate", dayMenu.day.format(DateTimeFormatter.ISO_DATE))
                    append("number", menuItem.number.toString())
                    append("author", jecnaClient.lastSuccessfulLoginAuth!!.username)
                    append("description", menuItem.description!!.rest)
                    append(
                        key = "image",
                        value = ChannelProvider(fileDescriptor.statSize) { imageChannel },
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/*")
                            append(HttpHeaders.ContentDisposition, "filename=image.jpg")
                        }
                    )
                }
            )
            withContext(Dispatchers.IO) {
                imageInputStream.close()
                fileDescriptor.close()
            }
        }
    }

    private fun getTmpFileUri(): Uri
    {
        val tmpFile = File.createTempFile("tmp_image_file", ".jpg", appContext.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(appContext, appContext.packageName + ".fileprovider", tmpFile)
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

    fun onTakeImageEventConsumed() = changeUiState(takeImageEvent = consumed())

    fun changeUiState(
        loading: Boolean = uiState.loading,
        isUploader: Boolean = uiState.isUploader,
        orderInProcess: Boolean = uiState.orderInProcess,
        menuPage: MenuPage? = uiState.menuPage,
        images: ImagesMap = uiState.images,
        takeImageEvent: StateEventWithContent<Uri> = uiState.takeImageEvent,
        snackBarMessageEvent: StateEventWithContent<String> = uiState.snackBarMessageEvent,
    )
    {
        uiState = uiState.copy(
            loading = loading,
            isUploader = isUploader,
            menuPage = menuPage,
            images = images,
            orderInProcess = orderInProcess,
            takeImageEvent = takeImageEvent,
            snackBarMessageEvent = snackBarMessageEvent,
        )
    }

    companion object
    {
        const val CANTEEN_IMAGES_HOST = "http://192.168.1.131:80"
    }
}