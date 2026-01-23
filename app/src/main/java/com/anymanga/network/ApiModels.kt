package com.anymanga.network

import com.google.gson.annotations.SerializedName

// Request Models
data class UrlSearchRequest(val url: String)
data class NameSearchRequest(val name: String)
data class FetchRequest(val url: String)

// Response Models
data class SourceResponse(
    val found: Boolean,
    val source: ServerSource?,
    val engine: String?,
    val api: ApiEndpoints?
)

data class ServerSource(
    val id: String,
    val name: String,
    val lang: String,
    val baseUrl: String,
    val nsfw: Boolean
)

data class ApiEndpoints(
    val engine: String,
    val endpoints: Map<String, EndpointConfig>?
)

data class EndpointConfig(
    val url: String,
    val method: String,
    val params: Map<String, String>? = null,
    val headers: Map<String, String>? = null
)

data class ChapterDataResponse(
    val success: Boolean,
    val url: String,
    val title: String,
    val contentLength: Int,
    val totalImages: Int,
    val chapterImages: List<String>,
    val preview: String? = null
)

data class StatsResponse(
    val totalExtensions: Int,
    val totalSources: Int,
    val byEngine: Map<String, Int>,
    val byLanguage: Map<String, Int>
)

data class SourceListResponse(
    val found: Boolean,
    val count: Int,
    val sources: List<ServerSource>
)
data class HtmlResponse(
    val success: Boolean,
    val url: String,
    val html: String
)
