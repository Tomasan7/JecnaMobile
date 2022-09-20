package me.tomasan7.jecnamobile.subscreen.state

import me.tomasan7.jecnaapi.data.article.NewsPage

data class NewsState(
    val newsPage: NewsPage? = null,
    val loading: Boolean = false
)