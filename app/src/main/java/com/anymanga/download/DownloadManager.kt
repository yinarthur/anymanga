package com.anymanga.download

import android.content.Context
import com.anymanga.data.AppDatabase
import com.anymanga.data.ChapterEntity
import com.anymanga.data.DownloadEntity
import com.anymanga.data.DownloadStatus
import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Chapter
import com.anymanga.data.SourceTemplateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadManager(
    private val context: Context,
    private val db: AppDatabase,
    private val client: OkHttpClient
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun enqueueDownload(mangaId: String, chapter: com.anymanga.model.Chapter, template: SourceTemplateEntity) {
        scope.launch {
            // Convert model.Chapter to data.ChapterEntity for DB
            val chapterEntity = ChapterEntity(
                id = chapter.url,
                mangaId = mangaId,
                title = chapter.name,
                url = chapter.url
            )
            val download = DownloadEntity(chapterEntity.id, mangaId, 0, DownloadStatus.PENDING)
            db.mangaDao().insertChapters(listOf(chapterEntity))
            db.mangaDao().updateDownload(download)
            startDownload(mangaId, chapterEntity, template)
        }
    }

    private suspend fun startDownload(mangaId: String, chapter: ChapterEntity, template: SourceTemplateEntity) {
        try {
            val engine = EngineRegistry.getEngineForTemplate(template)
            val pages = engine.getPages(template.baseUrl, chapter.url)
            val totalPages = pages.size
            val downloadFolder = File(context.filesDir, "downloads/$mangaId/${chapter.id}")
            if (!downloadFolder.exists()) downloadFolder.mkdirs()

            db.mangaDao().updateDownload(DownloadEntity(chapter.id, mangaId, 0, DownloadStatus.DOWNLOADING))

            pages.forEachIndexed { index, page ->
                val file = File(downloadFolder, "${index}.jpg")
                downloadPage(page.imageUrl, file)
                
                val progress = ((index + 1).toFloat() / totalPages * 100).toInt()
                db.mangaDao().updateDownload(DownloadEntity(chapter.id, mangaId, progress, DownloadStatus.DOWNLOADING))
            }

            db.mangaDao().updateDownload(DownloadEntity(chapter.id, mangaId, 100, DownloadStatus.COMPLETED))
        } catch (e: Exception) {
            db.mangaDao().updateDownload(DownloadEntity(chapter.id, mangaId, 0, DownloadStatus.FAILED))
        }
    }

    private fun downloadPage(url: String, targetFile: File) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return
            response.body?.byteStream()?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun clearCache() {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
    }
}
