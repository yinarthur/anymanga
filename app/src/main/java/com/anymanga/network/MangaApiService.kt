package com.anymanga.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MangaApiService {
    
    // Search source by URL
    @POST("search/url")
    suspend fun searchByUrl(@Body request: UrlSearchRequest): SourceResponse
    
    // Search source by name
    @POST("search/name")
    suspend fun searchByName(@Body request: NameSearchRequest): SourceListResponse
    
    // Get source API configuration
    @GET("source/{sourceId}/api")
    suspend fun getSourceApi(@Path("sourceId") sourceId: String): SourceResponse
    
    // Fetch chapter data (including images)
    @POST("test/fetch")
    suspend fun fetchChapterData(@Body request: FetchRequest): ChapterDataResponse
    
    // Get server statistics
    @GET("stats")
    suspend fun getStats(): StatsResponse
    
    // Fetch full HTML
    @POST("fetch/html")
    suspend fun fetchHtml(@Body request: FetchRequest): HtmlResponse

    // Health check
    @GET("../health")
    suspend fun healthCheck(): Map<String, String>
}
