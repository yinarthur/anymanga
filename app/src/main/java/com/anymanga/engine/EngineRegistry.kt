package com.anymanga.engine

import com.anymanga.data.SourceTemplateEntity
import com.anymanga.data.AppSettings
import okhttp3.OkHttpClient

/**
 * Registry for manga source engines.
 * Maps engineType from templates to specific engine implementations.
 */
object EngineRegistry {
    
    private val engines = mutableMapOf<String, BaseEngine>()
    var appSettings: AppSettings? = null

    // Shared client with proxy interceptor
    private val proxiedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(com.anymanga.network.ServerProxyInterceptor(
                serverUrlProvider = { com.anymanga.network.ApiConfig.getServerUrl() },
                useServerProvider = { appSettings?.useLocalServer ?: false }
            ))
            .build()
    }

    init {
        // Register default engines with the proxied client
        registerEngine("GENERIC", GenericHtmlEngine(proxiedClient))
        registerEngine("MADARA", MadaraEngine(proxiedClient))
    }

    fun registerEngine(type: String, engine: BaseEngine) {
        engines[type.uppercase()] = engine
    }

    fun getEngine(type: String): BaseEngine {
        // We no longer need to switch to "SERVER" engine because 
        // the proxiedClient handles the server-side fetching automatically.
        val finalEngine = engines[type.uppercase()] ?: engines["GENERIC"]!!
        
        android.util.Log.d("EngineRegistry", "Request: $type, ServerMode: ${appSettings?.useLocalServer} -> Selected: ${finalEngine::class.simpleName}")
        return finalEngine
    }

    fun getEngineForTemplate(template: SourceTemplateEntity): BaseEngine {
        return getEngine(template.engineType)
    }
}
