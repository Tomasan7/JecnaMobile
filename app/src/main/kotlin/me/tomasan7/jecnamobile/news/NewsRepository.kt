package me.tomasan7.jecnamobile.news

import me.tomasan7.jecnaapi.data.article.NewsPage

interface NewsRepository
{
    suspend fun getNewsPage(): NewsPage
}
