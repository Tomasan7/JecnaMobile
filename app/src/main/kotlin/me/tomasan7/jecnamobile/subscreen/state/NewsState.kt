package me.tomasan7.jecnamobile.subscreen.state

import me.tomasan7.jecnaapi.data.article.ArticlesPage

data class NewsState(
    val newsPage: ArticlesPage? = null,
    val loading: Boolean = false
)