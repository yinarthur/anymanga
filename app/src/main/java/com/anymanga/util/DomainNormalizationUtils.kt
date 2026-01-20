package com.anymanga.util

import java.net.URI
import java.util.Locale

object DomainNormalizationUtils {

    /**
     * Normalizes a URL or domain string to a canonical format:
     * - lowercase
     * - strip scheme (http/https)
     * - strip path, query, fragment
     * - strip leading 'www.'
     * - remove trailing slash
     * - handle punycode (basic java.net.URI does this)
     */
    fun normalize(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var normalized = input.trim().lowercase(Locale.ROOT)

        // Remove scheme if present
        if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7)
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8)
        }

        // Use URI to strip path, query, etc. if it looks like a full URL
        try {
            // Add a temporary scheme to help URI parse it if it was just a domain
            val uri = URI("http://$normalized")
            normalized = uri.host ?: normalized
        } catch (e: Exception) {
            // If it fails, fallback to simple string splitting
            normalized = normalized.split("/").first()
        }

        // Strip leading www.
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4)
        }

        // Final trimming of any trailing slashes (though URI.host should handle it)
        normalized = normalized.trimEnd('/')

        return normalized
    }
}
