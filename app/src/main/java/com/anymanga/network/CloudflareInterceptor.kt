package com.anymanga.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CloudflareInterceptor(private val context: Context) : Interceptor {

    private val handler = Handler(Looper.getMainLooper())
    private var userAgent: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Ensure user agent matches WebView to avoid secondary challenges
        val request = if (userAgent != null) {
            originalRequest.newBuilder().header("User-Agent", userAgent!!).build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(request)

        // Robust detection of Cloudflare challenges
        if (response.code in listOf(403, 503, 429)) {
            val responseString = response.peekBody(1024 * 10).string()
            val isCfChallenge = responseString.contains("cf-challenge") || 
                               responseString.contains("ray ID") || 
                               responseString.contains("just a moment")
            
            if (isCfChallenge) {
                response.close()
                val cookies = solveChallenge(request.url.toString())
                
                if (cookies.isNotEmpty()) {
                    val newRequest = request.newBuilder()
                        .header("Cookie", cookies)
                        .apply {
                            if (userAgent != null) header("User-Agent", userAgent!!)
                        }
                        .build()
                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    private fun solveChallenge(url: String): String {
        val latch = CountDownLatch(1)
        var cookies = ""

        handler.post {
            val webView = WebView(context)
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            
            if (userAgent == null) {
                userAgent = webView.settings.userAgentString
            }

            webView.webViewClient = object : WebViewClient() {
                private var timerStarted = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    val manager = CookieManager.getInstance()
                    val currentCookies = manager.getCookie(url)
                    
                    // CF clearance cookie usually takes a few seconds of JS execution
                    if (currentCookies?.contains("cf_clearance") == true) {
                        cookies = currentCookies
                        latch.countDown()
                        webView.destroy()
                    } else if (!timerStarted) {
                        // Sometimes the cookie isn't there immediately, wait a bit
                        timerStarted = true
                        handler.postDelayed({
                            val updatedCookies = manager.getCookie(url)
                            if (updatedCookies?.contains("cf_clearance") == true) {
                                cookies = updatedCookies
                                latch.countDown()
                                webView.destroy()
                            }
                        }, 5000)
                    }
                }
            }
            webView.loadUrl(url)
        }

        latch.await(45, TimeUnit.SECONDS)
        return cookies
    }
}
