package com.anymanga.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DomainNormalizationUtilsTest {

    @Test
    fun testNormalization() {
        val testCases = mapOf(
            "https://www.google.com/path?query=1" to "google.com",
            "http://example.com/" to "example.com",
            "WWW.MY-MANGA.NET" to "my-manga.net",
            "  https://MangaDex.org  " to "mangadex.org",
            "xn--mgbh0fb.com" to "xn--mgbh0fb.com", // Punycode preserved
            "sub.domain.com/page" to "sub.domain.com",
            "just-a-domain.io" to "just-a-domain.io"
        )

        testCases.forEach { (input, expected) ->
            assertEquals("Failed for input: $input", expected, DomainNormalizationUtils.normalize(input))
        }
    }

    @Test
    fun testEmptyInput() {
        assertEquals("", DomainNormalizationUtils.normalize(""))
        assertEquals("", DomainNormalizationUtils.normalize(null))
        assertEquals("", DomainNormalizationUtils.normalize("   "))
    }
}
