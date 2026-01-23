package com.anymanga.data

import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import com.anymanga.model.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Main repository for manga data.
 * Orchestrates fetching from various sources via engines.
 */
class MangaRepository(
    private val database: AppDatabase
) {
    private val sourceDao = database.sourceDao()

    /**
     * Fetches latest manga from all enabled sources.
     */
    suspend fun getLatestUpdates(): List<Pair<SourceTemplateEntity, List<Manga>>> = withContext(Dispatchers.IO) {
        val enabledSources = sourceDao.observeEnabledSources().first()
        val allUpdates = mutableListOf<Pair<SourceTemplateEntity, List<Manga>>>()
        
        enabledSources.forEach { source ->
            try {
                val engine = EngineRegistry.getEngineForTemplate(source)
                val latest = engine.getLatestManga(source.baseUrl, 1)
                if (latest.isNotEmpty()) {
                    allUpdates.add(source to latest)
                }
            } catch (e: Exception) {
                android.util.Log.e("MangaRepository", "Failed to fetch latest from ${source.name}: ${e.message}")
            }
        }
        allUpdates
    }

    /**
     * Searches manga across all enabled sources.
     */
    suspend fun searchManga(query: String): List<Pair<String, Manga>> = withContext(Dispatchers.IO) {
        val enabledSources = sourceDao.observeEnabledSources().first()
        val allResults = mutableListOf<Pair<String, Manga>>()
        
        enabledSources.forEach { source ->
            try {
                val engine = EngineRegistry.getEngineForTemplate(source)
                val results = engine.searchManga(source.baseUrl, query, 1)
                allResults.addAll(results.map { source.id to it })
            } catch (e: Exception) {
                android.util.Log.e("MangaRepository", "Search failed for ${source.name}: ${e.message}")
            }
        }
        allResults
    }

    /**
     * Gets detailed information for a specific manga.
     */
    suspend fun getMangaDetails(sourceId: String, mangaUrl: String): Result<Pair<Manga, List<Chapter>>> = withContext(Dispatchers.IO) {
        try {
            val source = sourceDao.getAllTemplates().first().find { it.id == sourceId }
                ?: return@withContext Result.failure(Exception("Source not found: $sourceId"))
            
            val engine = EngineRegistry.getEngineForTemplate(source)
            val details = engine.getMangaDetails(source.baseUrl, mangaUrl)
            val chapters = engine.getChapters(source.baseUrl, mangaUrl)
            
            Result.success(details to chapters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets pages for a specific chapter.
     */
    suspend fun getChapterPages(sourceId: String, chapterUrl: String): Result<List<Page>> = withContext(Dispatchers.IO) {
        try {
            val source = sourceDao.getAllTemplates().first().find { it.id == sourceId }
                ?: return@withContext Result.failure(Exception("Source not found: $sourceId"))
            
            val engine = EngineRegistry.getEngineForTemplate(source)
            val pages = engine.getPages(source.baseUrl, chapterUrl)
            Result.success(pages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
