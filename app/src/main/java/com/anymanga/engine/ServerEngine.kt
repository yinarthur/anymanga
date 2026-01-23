package com.anymanga.engine

import com.anymanga.data.ServerMangaRepository
import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import com.anymanga.model.Page

class ServerEngine(
    private val repository: ServerMangaRepository
) : BaseEngine {

    override suspend fun getLatestManga(baseUrl: String, page: Int): List<Manga> {
        // Server mode doesn't support "latest" - return empty
        return emptyList() 
    }

    override suspend fun searchManga(baseUrl: String, query: String, page: Int): List<Manga> {
        // In server mode, we search by URL (baseUrl is the manga site URL)
        val result = repository.searchMangaByUrl(baseUrl)
        return result.getOrNull()?.let { source ->
            // Return the source as a single "manga" result
            listOf(
                Manga(
                    title = source.name,
                    url = source.baseUrl,
                    thumbnailUrl = ""
                )
            )
        } ?: emptyList()
    }

    override suspend fun getMangaDetails(baseUrl: String, mangaUrl: String): Manga {
        // For server mode, manga details are minimal
        // The actual manga browsing happens on the website
        return Manga(
            title = "Browse on website",
            url = mangaUrl,
            thumbnailUrl = "",
            description = "This source is available. Visit the website to browse manga.",
            author = "",
            status = "Available"
        )
    }

    override suspend fun getChapters(baseUrl: String, mangaUrl: String): List<Chapter> {
        // Server mode: fetch chapters from the API
        val result = repository.getChapterData(mangaUrl)
        return result.getOrNull()?.chapterImages?.mapIndexed { index, _ ->
            Chapter(
                name = "Page ${index + 1}",
                url = mangaUrl
            )
        } ?: emptyList()
    }

    override suspend fun getPages(baseUrl: String, chapterUrl: String): List<Page> {
        // Fetch chapter images from server
        val result = repository.getChapterData(chapterUrl)
        return result.getOrNull()?.chapterImages?.mapIndexed { index, imageUrl ->
            Page(index, imageUrl)
        } ?: emptyList()
    }
}
