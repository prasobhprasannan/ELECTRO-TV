package com.example

import com.example.data.network.M3uParser
import com.example.data.network.StalkerPortalApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    @Test
    fun parseM3uContent_extractsChannelsAndAttributes() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="nasa.hd" tvg-name="NASA TV" tvg-logo="https://example.com/nasa.png" group-title="Science", NASA TV HD
            https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8
            #EXTINF:-1 tvg-id="bloomberg" group-title="News", Bloomberg TV
            https://liveprodupmx.akamaized.net/us/live.m3u8
        """.trimIndent()

        val channels = M3uParser.parseContent(sampleM3u, profileId = 99L)

        assertEquals(2, channels.size)

        val ch1 = channels[0]
        assertEquals("NASA TV HD", ch1.name)
        assertEquals("Science", ch1.groupTitle)
        assertEquals("nasa.hd", ch1.tvgId)
        assertEquals("https://example.com/nasa.png", ch1.logoUrl)
        assertEquals("https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8", ch1.streamUrl)
        assertEquals("m3u8", ch1.streamFormat)

        val ch2 = channels[1]
        assertEquals("Bloomberg TV", ch2.name)
        assertEquals("News", ch2.groupTitle)
    }

    @Test
    fun generateRandomMagMac_formatIsValid() {
        val mac = StalkerPortalApi.generateRandomMagMac()
        assertTrue(mac.startsWith("00:1A:79:"))
        assertEquals(17, mac.length)
        val parts = mac.split(":")
        assertEquals(6, parts.size)
    }
}
