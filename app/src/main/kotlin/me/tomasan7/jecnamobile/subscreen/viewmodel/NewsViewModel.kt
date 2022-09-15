package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tomasan7.jecnaapi.data.article.ArticleFile
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.repository.ArticlesRepository
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.state.NewsState
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val articlesRepository: ArticlesRepository,
    private val webClient: JecnaWebClient,
    @ApplicationContext
    private val appContext: Context
) : ViewModel()
{
    var uiState by mutableStateOf(NewsState())
        private set

    private var loadNewsJob: Job? = null

    init
    {
        loadNews()
    }

    @OptIn(InternalAPI::class)
    fun downloadAndOpenArticleFile(articleFile: ArticleFile)
    {
        viewModelScope.launch {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), articleFile.filename)
            try
            {
                webClient.query(articleFile.downloadPath).content.copyAndClose(file.writeChannel())
                withContext(Dispatchers.Main) {
                    openFile(file)
                }
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_download), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openFile(file: File)
    {
        val fileUri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.provider", file)
        val mime = appContext.contentResolver.getType(fileUri)
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.setDataAndType(fileUri, mime)
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try
        {
            appContext.startActivity(openIntent)
        }
        catch (e: ActivityNotFoundException)
        {
            Toast.makeText(appContext, appContext.getString(R.string.error_unable_to_open_file), Toast.LENGTH_LONG).show()
        }
    }

    fun loadNews()
    {
        uiState = uiState.copy(loading = true)

        loadNewsJob?.cancel()

        loadNewsJob = viewModelScope.launch {
            try
            {
                val news = articlesRepository.queryArticlesPage()
                uiState = uiState.copy(loading = false, newsPage = news)
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.unsupported_articles), Toast.LENGTH_LONG).show()
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