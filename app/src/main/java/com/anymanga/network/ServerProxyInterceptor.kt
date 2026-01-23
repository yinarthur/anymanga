package com.anymanga.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

/**
 * Interceptor that proxies requests through the AnyManga server.
 * Useful for bypassing Cloudflare or IP blocks.
 */
class ServerProxyInterceptor(
    private val serverUrlProvider: () -> String,
    private val useServerProvider: () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Only proxy if enabled and we have a server URL
        if (!useServerProvider() || serverUrlProvider().isBlank()) {
            return chain.proceed(request)
        }

        // If it's already a call to our server, don't proxy it again
        if (request.url.toString().contains(serverUrlProvider())) {
            return chain.proceed(request)
        }

        // Prepare the proxy request to our server
        val targetUrl = request.url.toString()
        val serverBaseUrl = serverUrlProvider().removeSuffix("/")
        // The server base URL from ApiConfig already includes /api/
        val proxyUrl = "$serverBaseUrl/fetch/html"

        val json = JSONObject()
        json.put("url", targetUrl)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val proxyRequest = Request.Builder()
            .url(proxyUrl)
            .post(body)
            .build()

        return try {
            val response = chain.proceed(proxyRequest)
            if (!response.isSuccessful) {
                return response // Return the error from server
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val html = jsonResponse.optString("html", "")
                
                // Create a fake response with the returned HTML
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(html.toResponseBody("text/html; charset=utf-8".toMediaType()))
                    .build()
            } else {
                // If server failed to fetch, return its error
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message(jsonResponse.optString("error", "Server proxy failed"))
                    .body(responseBody.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        } catch (e: Exception) {
            // Fallback to local if proxy fails? 
            // Better to return the error to let user know proxy is broken.
            throw IOException("Proxy error: ${e.message}")
        }
    }
}
