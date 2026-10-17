package com.example

import com.example.core.model.VodProgressEntry
import com.example.core.service.XtreamCatalogService
import com.example.core.storage.AppStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnexVisionUnitTest {

    @Test
    fun testUrlSanitization() {
        val testUrl1 = "http://http://server.net:8080/"
        var clean = testUrl1.trim()
        if (clean.startsWith("http://http://")) clean = clean.replace("http://http://", "http://")
        clean = clean.removeSuffix("/")
        assertEquals("http://server.net:8080", clean)
    }

    @Test
    fun testVodProgressFraction() {
        val entry = VodProgressEntry(
            streamId = "movie_1",
            title = "Cyber Odyssey",
            positionMs = 3600_000L,
            durationMs = 7200_000L
        )
        assertEquals(0.5f, entry.progressFraction, 0.01f)
    }

    @Test
    fun testM3UParser() = runBlocking {
        val catalogService = XtreamCatalogService()
        val sampleM3U = """
            #EXTM3U
            #EXTINF:-1 tvg-id="1" tvg-logo="http://logo.png" group-title="Cinema VIP",UNEX Premiere 4K
            http://stream.m3u8
        """.trimIndent()

        val parsed = catalogService.parseM3U(sampleM3U)
        assertEquals(1, parsed.size)
        assertEquals("UNEX Premiere 4K", parsed[0].name)
        assertEquals("http://stream.m3u8", parsed[0].streamUrl)
        assertEquals("Cinema VIP", parsed[0].categoryName)
    }

    @Test
    fun testInitialCatalogNotEmpty() {
        val catalogService = XtreamCatalogService()
        val (live, vod, series) = catalogService.getInitialCatalog()
        assertTrue(live.isNotEmpty())
        assertTrue(vod.isNotEmpty())
        assertTrue(series.isNotEmpty())
    }
}
