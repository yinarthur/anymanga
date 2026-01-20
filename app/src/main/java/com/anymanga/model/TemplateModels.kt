package com.anymanga.model

import kotlinx.serialization.Serializable

@Serializable
data class TemplatesIndex(
    val version: Int,
    val generatedAtEpoch: Long,
    val count: Int,
    val sourceIndexUrl: String,
    val sourceIndexSha256: String,
    val templatesSha256: String,
    val templates: List<Template>
)

@Serializable
data class Template(
    val id: String,
    val name: String,
    val domain: String,
    val baseUrl: String,
    val engineType: String,
    val lang: String,
    val isNsfw: Boolean = false,
    val hasCloudflare: Boolean = false,
    val isDead: Boolean = false,
    val updatedAtEpoch: Long = 0L
)

@Serializable
data class Manga(
    val title: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val status: String? = null,
    val genres: List<String> = emptyList()
)

@Serializable
data class Chapter(
    val name: String,
    val url: String,
    val dateUploaded: Long = 0L,
    val chapterNumber: Float = 0f,
    val scanlator: String? = null
)

@Serializable
data class Page(
    val index: Int,
    val imageUrl: String
)
