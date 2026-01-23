package com.anymanga.data

import com.anymanga.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerMangaRepository(private val apiService: MangaApiService) {
    
    suspend fun searchMangaByUrl(url: String): Result<ServerSource> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchByUrl(UrlSearchRequest(url))
                if (response.found && response.source != null) {
                    Result.success(response.source)
                } else {
                    Result.failure(Exception("Source not found in server archive"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getChapterData(url: String): Result<ChapterDataResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchChapterData(FetchRequest(url))
                if (response.success) {
                    Result.success(response)
                } else {
                    Result.failure(Exception("Failed to fetch chapter data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun testSource(url: String): Result<SourceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchByUrl(UrlSearchRequest(url))
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getServerStats(): Result<StatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStats()
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun fetchHtml(url: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchHtml(FetchRequest(url))
                if (response.success) {
                    Result.success(response.html)
                } else {
                    Result.failure(Exception("Failed to fetch HTML"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
