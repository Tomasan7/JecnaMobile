package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.ArticlesPage

data class ArticlesState(
    val articlesPage: ArticlesPage? = null,
    val loading: Boolean = false
)