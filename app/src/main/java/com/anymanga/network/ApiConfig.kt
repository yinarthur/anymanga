package com.anymanga.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    // Default: 10.0.2.2 is the special alias to your host loopback interface (localhost) for Android Emulator
    // For physical device with Ngrok: Use the Ngrok HTTPS URL (e.g., "https://abc123.ngrok.io/api/")
    // For physical device on same WiFi: Use your computer's local IP (e.g., "http://192.168.1.100:3000/api/")
    private var baseUrl = "http://10.0.2.2:3000/api/"
    
    /**
     * Update the server URL dynamically.
     * Call this from Settings when user changes the server URL.
     */
    fun setServerUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
    }
    
    fun getServerUrl(): String = baseUrl
    
    fun createApiService(): MangaApiService {
        val client = OkHttpClient.Builder()
            .build()
            
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaApiService::class.java)
    }
}
