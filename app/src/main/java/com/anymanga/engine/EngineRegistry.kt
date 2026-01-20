package com.anymanga.engine

import com.anymanga.data.SourceTemplateEntity

/**
 * Registry for manga source engines.
 * Maps engineType from templates to specific engine implementations.
 */
object EngineRegistry {
    
    private val engines = mutableMapOf<String, BaseEngine>()

    init {
        // Register default engines
        registerEngine("GENERIC", GenericHtmlEngine())
        registerEngine("MADARA", MadaraEngine())
    }

    fun registerEngine(type: String, engine: BaseEngine) {
        engines[type.uppercase()] = engine
    }

    fun getEngine(type: String): BaseEngine {
        return engines[type.uppercase()] ?: engines["GENERIC"]!!
    }

    fun getEngineForTemplate(template: SourceTemplateEntity): BaseEngine {
        return getEngine(template.engineType)
    }
}
