package com.anymanga.engine

import com.anymanga.data.AppSettings
import com.anymanga.data.SourceTemplateEntity
import com.anymanga.data.ServerMangaRepository

/**
 * Smart engine selector that chooses between Local Engines and Server Engine.
 * If Server Mode is enabled, it prioritizes AnyServer for supported sources.
 */
class EngineSelector(
    private val localRegistry: EngineRegistry,
    private val serverEngine: ServerEngine
) {
    fun select(template: SourceTemplateEntity, settings: AppSettings): BaseEngine {
        return if (settings.useLocalServer) {
            // In Server Mode, use ServerEngine
            // Future logic: Only use server if the source is supported by server
            serverEngine
        } else {
            // Local Mode
            localRegistry.getEngineForTemplate(template)
        }
    }
}
