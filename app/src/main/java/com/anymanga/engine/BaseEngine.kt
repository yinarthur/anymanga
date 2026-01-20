package com.anymanga.engine

import com.anymanga.model.Manga
import com.anymanga.model.Chapter
import com.anymanga.model.Page

interface BaseEngine {
    suspend fun getLatestManga(baseUrl: String, page: Int): List<Manga>
    suspend fun searchManga(baseUrl: String, query: String, page: Int): List<Manga>
    suspend fun getMangaDetails(baseUrl: String, mangaUrl: String): Manga
    suspend fun getChapters(baseUrl: String, mangaUrl: String): List<Chapter>
    suspend fun getPages(baseUrl: String, chapterUrl: String): List<Page>
}

class GenericHtmlEngine : BaseEngine {
    override suspend fun getLatestManga(baseUrl: String, page: Int): List<Manga> = emptyList()
    override suspend fun searchManga(baseUrl: String, query: String, page: Int): List<Manga> = emptyList()
    override suspend fun getMangaDetails(baseUrl: String, mangaUrl: String): Manga {
        return Manga(url = mangaUrl, title = "Manga", thumbnailUrl = "")
    }
    override suspend fun getChapters(baseUrl: String, mangaUrl: String): List<Chapter> = emptyList()
    override suspend fun getPages(baseUrl: String, chapterUrl: String): List<Page> = emptyList()
}
