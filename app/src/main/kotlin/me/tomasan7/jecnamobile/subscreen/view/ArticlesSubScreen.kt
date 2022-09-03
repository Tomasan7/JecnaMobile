package me.tomasan7.jecnamobile.subscreen.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ireward.htmlcompose.HtmlText
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.Article
import me.tomasan7.jecnaapi.data.ArticleFile
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.ArticlesSubScreenViewModel
import me.tomasan7.jecnamobile.ui.component.Card
import java.time.format.DateTimeFormatter
import java.util.*

@SubScreensNavGraph
@Destination
@Composable
fun ArticlesSubScreen(
    viewModel: ArticlesSubScreenViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadArticles() }
    ) {
        Column(
            modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.articlesPage != null)
                uiState.articlesPage.articles.forEach { article ->
                    Article(
                        article = article,
                        onArticleFileClick = { viewModel.downloadAndOpenArticleFile(it) }
                    )
                }
        }
    }
}

@Composable
private fun Article(
    article: Article,
    onArticleFileClick: (ArticleFile) -> Unit,
    modifier: Modifier = Modifier
)
{
    Card(
        title = {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier
    ) {
        val context = LocalContext.current

        HtmlText(
            text = article.htmlContent,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            linkClicked = {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(it)
                startActivity(context, intent, null)
            }
        )

        if (article.files.isNotEmpty())
        {
            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
            )

            article.files.forEach { articleFile ->
                ArticleFile(
                    articleFile = articleFile,
                    onClick = { onArticleFileClick(articleFile) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "${article.date.format(DATE_FORMATTER)} | ${article.author}",
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}

@Composable
private fun ArticleFile(
    articleFile: ArticleFile,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
)
{
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Icon(Icons.Default.Description, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            "${articleFile.label} (${articleFile.fileExtension})",
            textDecoration = TextDecoration.Underline
        )
    }
}

val DATE_FORMATTER = DateTimeFormatter.ofPattern("d. MMMM", Locale.forLanguageTag("cs-CZ"))