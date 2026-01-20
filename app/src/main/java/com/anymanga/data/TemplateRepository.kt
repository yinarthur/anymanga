package com.anymanga.data

import com.anymanga.model.Template
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TemplateRepository(
    private val updater: TemplatesUpdater,
    private val sourceDao: SourceDao
) {

    /**
     * Synchronizes remote templates with the local database.
     * Uses TemplatesUpdater for secure fetching (ETag + SHA256).
     */
    suspend fun syncRemoteTemplates(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val index = updater.updateTemplates()
            if (index != null) {
                // Map Template models to SourceTemplateEntity
                val entities = index.templates.map { template ->
                    template.toEntity()
                }

                // Upsert to database
                sourceDao.upsertTemplates(entities)
                
                // Cleanup old templates not in the current index
                sourceDao.deleteOldTemplates(entities.map { it.id })
                
                Result.success(Unit)
            } else {
                // Result.success(Unit) because null might just mean 304 Not Modified
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes all available templates from the database.
     */
    fun observeAllTemplates(): Flow<List<SourceTemplateEntity>> {
        return sourceDao.getAllTemplates()
    }

    /**
     * Observes templates with their enabled/pinned status.
     */
    fun observeTemplatesWithStatus(): Flow<List<SourceWithStatus>> {
        return sourceDao.observeTemplatesWithStatus()
    }

    /**
     * Enables or disables a source.
     */
    suspend fun setSourceEnabled(domain: String, enabled: Boolean) {
        sourceDao.setEnabled(domain, enabled)
    }

    private fun Template.toEntity() = SourceTemplateEntity(
        id = id,
        name = name,
        domain = domain,
        baseUrl = baseUrl,
        engineType = engineType,
        lang = lang,
        isNsfw = isNsfw,
        hasCloudflare = hasCloudflare,
        isDead = isDead,
        lastUpdate = updatedAtEpoch
    )
}
