package me.tomasan7.jecnamobile.subscreen.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ireward.htmlcompose.HtmlText
import com.ramcosta.composedestinations.annotation.Destination
import me.tomasan7.jecnaapi.data.article.Article
import me.tomasan7.jecnaapi.data.article.ArticleFile
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.SubScreensNavGraph
import me.tomasan7.jecnamobile.subscreen.viewmodel.ArticlesViewModel
import me.tomasan7.jecnamobile.ui.component.Card
import me.tomasan7.jecnamobile.ui.theme.label_dark
import me.tomasan7.jecnamobile.ui.theme.label_light
import java.time.format.DateTimeFormatter

@SubScreensNavGraph
@Destination
@Composable
fun ArticlesSubScreen(
    viewModel: ArticlesViewModel = hiltViewModel()
)
{
    val uiState = viewModel.uiState

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(uiState.loading),
        onRefresh = { viewModel.loadArticles() }
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.articlesPage != null)
                items(uiState.articlesPage.articles, { it.hashCode() }) { article ->
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
            article.files.forEach { articleFile ->
                Spacer(Modifier.height(10.dp))
                ArticleFile(
                    articleFile = articleFile,
                    onClick = { onArticleFileClick(articleFile) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

        Spacer(Modifier.height(10.dp))

        Text(
            text = if (article.schoolOnly)
                "${article.date.format(DATE_FORMATTER)} | ${article.author}"
            else
                "${article.date.format(DATE_FORMATTER)} | ${article.author} | ${stringResource(R.string.article_school_only)}",
            fontSize = 12.sp,
            color = if (isSystemInDarkTheme()) label_dark else label_light
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
    val surfaceShape = RoundedCornerShape(4.dp)

    Surface(
        tonalElevation = 4.dp,
        shape = surfaceShape,
        modifier = modifier.clip(shape = RoundedCornerShape(4.dp)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(5.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append(articleFile.label)

                    withStyle(SpanStyle(fontSize = 10.sp, color = if (isSystemInDarkTheme()) label_dark else label_light)) {
                        append("." + articleFile.fileExtension)
                    }
                }
            )
        }
    }
}

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d. MMMM")