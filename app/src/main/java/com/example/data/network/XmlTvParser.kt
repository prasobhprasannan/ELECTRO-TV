package com.example.data.network

import android.util.Xml
import com.example.data.model.EpgProgramEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

object XmlTvParser {

    private val httpClient: OkHttpClient
        get() = try {
            AppNetworkClient.getOkHttpClient(com.example.IptvApplication.instance)
        } catch (_: Exception) {
            defaultHttpClient
        }

    private val defaultHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val xmltvDateFormats = listOf(
        SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US),
        SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    )

    private fun parseXmltvDate(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val clean = raw.trim()
        for (fmt in xmltvDateFormats) {
            try {
                val date = fmt.parse(clean)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    suspend fun parseFromUrl(url: String, maxLimit: Int = 1000): List<EpgProgramEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmarters/1.0")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download EPG (HTTP ${response.code})")
        }

        val rawStream = response.body?.byteStream() ?: throw Exception("Empty EPG response")
        val stream: InputStream = if (url.endsWith(".gz", ignoreCase = true)) {
            GZIPInputStream(rawStream)
        } else {
            rawStream
        }

        parseStream(stream, maxLimit)
    }

    fun parseStream(stream: InputStream, maxLimit: Int = 1000): List<EpgProgramEntity> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        val programs = mutableListOf<EpgProgramEntity>()
        var eventType = parser.eventType

        var currentChannel: String? = null
        var currentStart: Long = 0
        var currentStop: Long = 0
        var currentTitle: String = ""
        var currentDesc: String = ""
        var currentCategory: String = ""
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT && programs.size < maxLimit) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "programme") {
                        currentChannel = parser.getAttributeValue(null, "channel")
                        val startAttr = parser.getAttributeValue(null, "start")
                        val stopAttr = parser.getAttributeValue(null, "stop")
                        currentStart = parseXmltvDate(startAttr)
                        currentStop = parseXmltvDate(stopAttr)
                        currentTitle = ""
                        currentDesc = ""
                        currentCategory = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "title" -> currentTitle = text
                            "desc" -> currentDesc = text
                            "category" -> currentCategory = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val endTag = parser.name
                    if (endTag == "programme" && !currentChannel.isNullOrEmpty() && currentTitle.isNotEmpty()) {
                        if (currentStop > currentStart) {
                            programs.add(
                                EpgProgramEntity(
                                    id = "${currentChannel}_${currentStart}",
                                    channelTvgId = currentChannel,
                                    title = currentTitle,
                                    description = currentDesc,
                                    startTimeEpoch = currentStart,
                                    endTimeEpoch = currentStop,
                                    category = currentCategory
                                )
                            )
                        }
                        currentChannel = null
                    }
                    currentTag = null
                }
            }
            eventType = parser.next()
        }

        return programs
    }
}
