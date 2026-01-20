package com.anymanga.data

import android.content.Context
import com.anymanga.model.TemplatesIndex
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class TemplatesUpdater(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }



    /**
     * Updates the local templates if a newer version is available.
     * Verified by ETag and SHA256.
     * @return TemplatesIndex if updated or loaded from cache, null if no update needed or error.
     */
    suspend fun updateTemplates(): TemplatesIndex? {
        val repoUrl = preferencesManager.userRepoUrl.first()
        if (repoUrl.isNullOrBlank()) {
             // No repository configured by user yet
             return null
        }
        
        val templatesUrl = repoUrl
        val sha256Url = "$repoUrl.sha256"

        val currentEtag = preferencesManager.getTemplatesEtag()
        
        // 1. Fetch SHA256 first for integrity check later
        val expectedSha256 = fetchRemoteSha256(sha256Url) ?: return null

        // 2. Fetch Templates with ETag
        val request = Request.Builder()
            .url(templatesUrl)
            .apply {
                if (currentEtag != null) {
                    header("If-None-Match", currentEtag)
                }
            }
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 304) {
                // No update needed based on ETag
                return null
            }

            if (!response.isSuccessful) {
                return null
            }

            val body = response.body ?: return null
            val etag = response.header("ETag")
            
            // 3. Verify Integrity
            val bytes = body.bytes()
            if (!verifySha256(bytes, expectedSha256)) {
                // Integrity check failed!
                return null
            }

            // 4. Parse and Save
            return try {
                val index = json.decodeFromString<TemplatesIndex>(String(bytes))
                saveTemplatesToLocal(bytes)
                if (etag != null) {
                    preferencesManager.saveTemplatesEtag(etag)
                }
                index
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun fetchRemoteSha256(url: String): String? {
        val request = Request.Builder().url(url).build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.trim()?.split(" ")?.firstOrNull()
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun verifySha256(data: ByteArray, expected: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        val hexString = hash.joinToString("") { "%02x".format(it) }
        return hexString.equals(expected, ignoreCase = true)
    }

    private suspend fun saveTemplatesToLocal(data: ByteArray) {
        val file = File(context.filesDir, "templates.min.json")
        file.writeBytes(data)
        preferencesManager.saveLastTemplatesUpdate(System.currentTimeMillis())
    }

    fun getLocalTemplates(): TemplatesIndex? {
        val file = File(context.filesDir, "templates.min.json")
        if (!file.exists()) return null
        return try {
            json.decodeFromString<TemplatesIndex>(file.readText())
        } catch (e: Exception) {
            null
        }
    }
}
