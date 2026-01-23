package com.anymanga.util

import android.util.Patterns
import java.net.URL

/**
 * Utility class for validating and normalizing URLs.
 */
object UrlValidator {

    /**
     * Checks if a string is a valid URL.
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            Patterns.WEB_URL.matcher(url).matches() && 
                    (url.startsWith("http://") || url.startsWith("https://"))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Normalizes a URL by ensuring it has a protocol and removing trailing slashes.
     */
    fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }
        return normalized.removeSuffix("/")
    }

    /**
     * Checks if a URL points to a JSON file (basic check).
     */
    fun isJsonUrl(url: String): Boolean {
        return url.lowercase().endsWith(".json") || url.lowercase().contains("json")
    }
}
