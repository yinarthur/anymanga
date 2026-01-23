package com.anymanga.engine

import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import com.anymanga.model.Page
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Smart Generic Engine with heuristic-based parsing.
 * Attempts to intelligently detect and parse manga sites without specific engine knowledge.
 */
class GenericHtmlEngine(private val client: OkHttpClient = OkHttpClient()) : BaseEngine {

    override suspend fun getLatestManga(baseUrl: String, page: Int): List<Manga> {
        val possibleUrls = listOf(
            "$baseUrl/latest",
            "$baseUrl/manga",
            "$baseUrl/series",
            "$baseUrl/browse",
            "$baseUrl/directory",
            "$baseUrl/list",
            "$baseUrl/page/$page"
        )

        for (url in possibleUrls) {
            try {
                val doc = fetchDocument(url) ?: continue
                val results = extractMangaList(doc, baseUrl)
                if (results.isNotEmpty()) return results
            } catch (e: Exception) {
                continue
            }
        }

        return emptyList()
    }

    override suspend fun searchManga(baseUrl: String, query: String, page: Int): List<Manga> {
        val possibleUrls = listOf(
            "$baseUrl/search?q=$query",
            "$baseUrl/?s=$query",
            "$baseUrl/search/$query",
            "$baseUrl/find/$query"
        )

        for (url in possibleUrls) {
            try {
                val doc = fetchDocument(url) ?: continue
                val results = extractMangaList(doc, baseUrl)
                if (results.isNotEmpty()) return results
            } catch (e: Exception) {
                continue
            }
        }

        return emptyList()
    }

    override suspend fun getMangaDetails(baseUrl: String, mangaUrl: String): Manga {
        return try {
            val doc = fetchDocument(mangaUrl) ?: throw Exception("Failed to load")
            
            Manga(
                title = extractTitle(doc),
                url = mangaUrl,
                thumbnailUrl = extractThumbnail(doc),
                description = extractDescription(doc),
                author = extractAuthor(doc),
                status = extractStatus(doc)
            )
        } catch (e: Exception) {
            Manga(url = mangaUrl, title = "Error", thumbnailUrl = "")
        }
    }

    override suspend fun getChapters(baseUrl: String, mangaUrl: String): List<Chapter> {
        return try {
            val doc = fetchDocument(mangaUrl) ?: return emptyList()
            extractChapterList(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPages(baseUrl: String, chapterUrl: String): List<Page> {
        return try {
            val doc = fetchDocument(chapterUrl) ?: return emptyList()
            extractPageList(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper methods

    private fun fetchDocument(url: String): Document? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val html = response.body?.string() ?: return null
            Jsoup.parse(html, url)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractMangaList(doc: Document, baseUrl: String): List<Manga> {
        val selectors = listOf(
            ".manga-item",
            ".manga",
            ".series",
            ".item",
            "article",
            ".post",
            "[class*=manga]",
            "[class*=series]",
            "[class*=comic]"
        )

        for (selector in selectors) {
            val elements = doc.select(selector)
            if (elements.size >= 3) {
                val results = elements.mapNotNull { extractMangaFromElement(it, baseUrl) }
                if (results.isNotEmpty()) return results
            }
        }

        return emptyList()
    }

    private fun extractMangaFromElement(element: Element, baseUrl: String): Manga? {
        val link = element.select("a[href]").firstOrNull() ?: return null
        val title = link.text().takeIf { it.isNotBlank() }
            ?: link.attr("title").takeIf { it.isNotBlank() }
            ?: element.select("h1, h2, h3, h4").text().takeIf { it.isNotBlank() }
            ?: return null

        val url = link.absUrl("href").takeIf { it.isNotBlank() } ?: return null
        val thumbnail = element.select("img").firstOrNull()?.let {
            it.absUrl("src").takeIf { url -> url.isNotBlank() }
                ?: it.absUrl("data-src").takeIf { url -> url.isNotBlank() }
        }

        return Manga(title = title, url = url, thumbnailUrl = thumbnail)
    }

    private fun extractTitle(doc: Document): String {
        val selectors = listOf(
            "h1.title",
            "h1.manga-title",
            "h1.series-title",
            ".post-title h1",
            "h1",
            "h2.title"
        )

        for (selector in selectors) {
            val text = doc.select(selector).firstOrNull()?.text()
            if (!text.isNullOrBlank()) return text
        }

        return "Unknown"
    }

    private fun extractThumbnail(doc: Document): String? {
        val selectors = listOf(
            ".manga-cover img",
            ".series-thumb img",
            ".summary_image img",
            ".thumbnail img",
            "img[class*=cover]",
            "img[class*=thumb]"
        )

        for (selector in selectors) {
            val img = doc.select(selector).firstOrNull()
            val url = img?.absUrl("src")?.takeIf { it.isNotBlank() }
                ?: img?.absUrl("data-src")?.takeIf { it.isNotBlank() }
            if (url != null) return url
        }

        return null
    }

    private fun extractDescription(doc: Document): String? {
        val selectors = listOf(
            ".description",
            ".summary",
            ".synopsis",
            "[class*=description]",
            "[class*=summary]",
            "p.description"
        )

        for (selector in selectors) {
            val text = doc.select(selector).firstOrNull()?.text()
            if (!text.isNullOrBlank()) return text
        }

        return null
    }

    private fun extractAuthor(doc: Document): String? {
        val selectors = listOf(
            ".author",
            ".author-content",
            "[class*=author]",
            "span.author"
        )

        for (selector in selectors) {
            val text = doc.select(selector).firstOrNull()?.text()
            if (!text.isNullOrBlank()) return text
        }

        return null
    }

    private fun extractStatus(doc: Document): String? {
        val selectors = listOf(
            ".status",
            ".post-status",
            "[class*=status]"
        )

        for (selector in selectors) {
            val text = doc.select(selector).firstOrNull()?.text()
            if (!text.isNullOrBlank()) return text
        }

        return null
    }

    private fun extractChapterList(doc: Document): List<Chapter> {
        val selectors = listOf(
            ".chapter-list li",
            ".chapters li",
            ".chapter",
            "[class*=chapter]",
            "li a[href*=chapter]"
        )

        for (selector in selectors) {
            val elements = doc.select(selector)
            if (elements.size >= 1) {
                val chapters = elements.mapNotNull { element ->
                    val link = element.select("a[href]").firstOrNull() ?: return@mapNotNull null
                    val name = link.text().takeIf { it.isNotBlank() } ?: "Chapter"
                    val url = link.absUrl("href").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    Chapter(name = name, url = url)
                }
                if (chapters.isNotEmpty()) return chapters
            }
        }

        return emptyList()
    }

    private fun extractPageList(doc: Document): List<Page> {
        val selectors = listOf(
            ".reading-content img",
            ".reader-content img",
            ".chapter-content img",
            "#chapter-reader img",
            "[class*=reader] img",
            "[class*=content] img"
        )

        for (selector in selectors) {
            val images = doc.select(selector)
            if (images.size >= 1) {
                val pages = images.mapIndexedNotNull { index, img ->
                    val url = img.absUrl("src").takeIf { it.isNotBlank() }
                        ?: img.absUrl("data-src").takeIf { it.isNotBlank() }
                        ?: return@mapIndexedNotNull null
                    Page(index = index, imageUrl = url)
                }
                if (pages.isNotEmpty()) return pages
            }
        }

        return emptyList()
    }
}
