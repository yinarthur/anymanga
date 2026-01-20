package com.anymanga.engine

import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import com.anymanga.model.Page
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class MadaraEngine(private val client: OkHttpClient = OkHttpClient()) : BaseEngine {

    override suspend fun getLatestManga(baseUrl: String, page: Int): List<Manga> {
        val url = "$baseUrl/manga/page/$page/"
        val request = Request.Builder().url(url).build()
        
        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: return emptyList()
            val doc = Jsoup.parse(response)
            
            doc.select(".manga-item, .page-item-detail").map { element ->
                val titleElement = element.select("h3 a, h4 a").first()
                val imageElement = element.select("img").first()
                
                Manga(
                    title = titleElement?.text() ?: "Unknown",
                    url = titleElement?.absUrl("href") ?: "",
                    thumbnailUrl = imageElement?.absUrl("src") ?: imageElement?.absUrl("data-src")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchManga(baseUrl: String, query: String, page: Int): List<Manga> {
        val url = "$baseUrl/?s=$query&post_type=wp-manga"
        val request = Request.Builder().url(url).build()
        
        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: return emptyList()
            val doc = Jsoup.parse(response)
            
            doc.select(".c-tabs-item__content, .search-wrap .row").map { element ->
                val titleElement = element.select(".post-title a, h3 a").first()
                val imageElement = element.select("img").first()
                
                Manga(
                    title = titleElement?.text() ?: "Unknown",
                    url = titleElement?.absUrl("href") ?: "",
                    thumbnailUrl = imageElement?.absUrl("src") ?: imageElement?.absUrl("data-src")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getMangaDetails(baseUrl: String, mangaUrl: String): Manga {
        val request = Request.Builder().url(mangaUrl).build()
        
        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: throw Exception("Failed to load details")
            val doc = Jsoup.parse(response)
            
            Manga(
                title = doc.select(".post-title h1").text(),
                url = mangaUrl,
                thumbnailUrl = doc.select(".summary_image img").attr("abs:src"),
                description = doc.select(".description-summary, .manga-excerpt").text(),
                author = doc.select(".author-content").text(),
                status = doc.select(".post-status .summary-content").text()
            )
        } catch (e: Exception) {
            Manga(url = mangaUrl, title = "Error", thumbnailUrl = "")
        }
    }

    override suspend fun getChapters(baseUrl: String, mangaUrl: String): List<Chapter> {
        val request = Request.Builder().url(mangaUrl).build()
        
        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: return emptyList()
            val doc = Jsoup.parse(response)
            
            doc.select(".wp-manga-chapter").map { element ->
                val link = element.select("a").first()
                Chapter(
                    name = link?.text()?.trim() ?: "Chapter",
                    url = link?.absUrl("href") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPages(baseUrl: String, chapterUrl: String): List<Page> {
        val request = Request.Builder().url(chapterUrl).build()
        
        return try {
            val response = client.newCall(request).execute().use { it.body?.string() } ?: return emptyList()
            val doc = Jsoup.parse(response)
            
            doc.select(".reading-content img").mapIndexed { index, element ->
                Page(
                    index = index,
                    imageUrl = element.absUrl("src").trim()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
