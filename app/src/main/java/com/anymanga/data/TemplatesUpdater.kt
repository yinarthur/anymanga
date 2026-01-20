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
    /**
     * Updates the local templates if a newer version is available.
     * Verified by ETag and SHA256.
     * @return TemplatesIndex if updated or loaded from cache, null if no update needed or error.
     */
    suspend fun updateTemplates(): TemplatesIndex? {
        val repoUrl = preferencesManager.userRepoUrl.first()
        android.util.Log.d("TemplatesUpdater", "Update requested. Repo URL: $repoUrl")
        
        if (repoUrl.isNullOrBlank()) {
             android.util.Log.e("TemplatesUpdater", "No repository URL configured")
             return null
        }
        
        val templatesUrl = repoUrl
        val sha256Url = "$repoUrl.sha256?t=${System.currentTimeMillis()}"

        val currentEtag = preferencesManager.getTemplatesEtag()
        
        // 1. Fetch SHA256 first for integrity check later
        val expectedSha256 = fetchRemoteSha256(sha256Url)
        if (expectedSha256 == null) {
            android.util.Log.e("TemplatesUpdater", "Failed to fetch SHA256 from $sha256Url")
            return null
        }

        // 2. Fetch Templates with ETag
        val request = Request.Builder()
            .url(templatesUrl)
            .apply {
                if (currentEtag != null) {
                    header("If-None-Match", currentEtag)
                }
            }
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                android.util.Log.d("TemplatesUpdater", "Fetch response: ${response.code}")
                
                if (response.code == 304) {
                    android.util.Log.d("TemplatesUpdater", "Templates up to date (304 Not Modified)")
                    return getLocalTemplates() // Return local if not modified
                }

                if (!response.isSuccessful) {
                    android.util.Log.e("TemplatesUpdater", "Fetch failed with code: ${response.code}")
                    return null
                }

                val body = response.body ?: return null
                val etag = response.header("ETag")
                
                // 3. Verify Integrity
                val bytes = body.bytes()
                if (!verifySha256(bytes, expectedSha256)) {
                    android.util.Log.e("TemplatesUpdater", "SHA256 mismatch! integrity check failed.")
                    return null
                }

                // 4. Parse and Save
                val index = json.decodeFromString<TemplatesIndex>(String(bytes))
                android.util.Log.d("TemplatesUpdater", "Parsed ${index.count} templates successfully")
                saveTemplatesToLocal(bytes)
                if (etag != null) {
                    preferencesManager.saveTemplatesEtag(etag)
                }
                index
            }
        } catch (e: Exception) {
            android.util.Log.e("TemplatesUpdater", "Exception during update: ${e.message}", e)
            null
        }
    }

    /**
     * Downloads templates and populates the Room database.
     */
    suspend fun syncWithDatabase(database: AppDatabase): Boolean {
        android.util.Log.d("TemplatesUpdater", "Starting syncWithDatabase...")
        val index = updateTemplates() ?: getLocalTemplates() 
        
        if (index == null) {
            android.util.Log.e("TemplatesUpdater", "Failed to get templates (update failed and no local cache)")
            return false
        }
        
        return try {
            android.util.Log.d("TemplatesUpdater", "Mapping ${index.templates.size} templates to entities...")
            val entities = index.templates.map { template ->
                SourceTemplateEntity(
                    id = template.id,
                    name = template.name,
                    domain = template.domain,
                    baseUrl = template.baseUrl,
                    engineType = template.engineType,
                    lang = template.lang,
                    isNsfw = template.isNsfw,
                    hasCloudflare = template.hasCloudflare,
                    isDead = template.isDead,
                    lastUpdate = System.currentTimeMillis()
                )
            }
            database.sourceDao().upsertTemplates(entities)
            android.util.Log.d("TemplatesUpdater", "Inserted ${entities.size} templates into DB")
            
            // Optional: Cleanup old templates not in the new index
            if (entities.isNotEmpty()) {
                database.sourceDao().deleteOldTemplates(entities.map { it.id })
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("TemplatesUpdater", "Database sync failed: ${e.message}", e)
            false
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
