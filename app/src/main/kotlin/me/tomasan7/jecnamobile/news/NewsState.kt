package me.tomasan7.jecnamobile.news

import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import me.tomasan7.jecnaapi.data.article.NewsPage
import me.tomasan7.jecnaapi.data.grade.GradesPage
import java.time.Instant

data class NewsState(
    val loading: Boolean = false,
    val newsPage: NewsPage? = null,
    val lastUpdateTimestamp: Instant? = null,
    val isCache: Boolean = false,
    val snackBarMessageEvent: StateEventWithContent<String> = consumed()
)