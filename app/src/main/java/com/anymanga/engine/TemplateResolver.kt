package com.anymanga.engine

import com.anymanga.data.SourceDao
import com.anymanga.data.SourceTemplateEntity
import com.anymanga.util.DomainNormalizationUtils
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class TemplateResolver(
    private val sourceDao: SourceDao,
    private val client: OkHttpClient = OkHttpClient()
) {

    /**
     * Attempts to find or create a template for a given URL.
     * 1. Try exact domain match in local database.
     * 2. Try auto-detection if not found.
     */
    suspend fun resolve(inputUrl: String): SourceTemplateEntity? {
        val normalizedDomain = DomainNormalizationUtils.normalize(inputUrl)
        if (normalizedDomain.isEmpty()) return null

        // 1. Seek in local DB
        val existing = sourceDao.getAllTemplates().first().find { it.domain == normalizedDomain }
        if (existing != null) return existing

        // 2. Auto-Detection
        return autoDetect(inputUrl)
    }

    private suspend fun autoDetect(url: String): SourceTemplateEntity? {
        val fullUrl = if (!url.startsWith("http")) "https://$url" else url
        val request = Request.Builder().url(fullUrl).build()

        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: return null
            val doc = Jsoup.parse(response)
            
            // Check for Madara fingerprint
            val isMadara = doc.select("link[href*='wp-manga'], script[src*='wp-manga'], body.wp-manga-template").isNotEmpty() ||
                           doc.html().contains("wp-manga", ignoreCase = true)
            
            val domain = DomainNormalizationUtils.normalize(url)
            
            if (isMadara) {
                SourceTemplateEntity(
                    id = "auto-madara-$domain",
                    name = domain.replaceFirstChar { it.uppercase() },
                    domain = domain,
                    baseUrl = fullUrl.trimEnd('/'),
                    engineType = "Madara",
                    lang = "unknown",
                    isNsfw = false
                )
            } else {
                // Fallback to Generic
                SourceTemplateEntity(
                    id = "auto-generic-$domain",
                    name = domain.replaceFirstChar { it.uppercase() },
                    domain = domain,
                    baseUrl = fullUrl.trimEnd('/'),
                    engineType = "Generic",
                    lang = "unknown",
                    isNsfw = false
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
