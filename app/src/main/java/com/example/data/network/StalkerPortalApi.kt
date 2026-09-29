package com.example.data.network

import com.example.data.model.ChannelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Random
import java.util.concurrent.TimeUnit

data class StalkerAuthResult(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val channelCount: Int = 0
)

object StalkerPortalApi {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun generateRandomMagMac(): String {
        val random = Random()
        val b3 = String.format("%02X", random.nextInt(256))
        val b4 = String.format("%02X", random.nextInt(256))
        val b5 = String.format("%02X", random.nextInt(256))
        return "00:1A:79:$b3:$b4:$b5"
    }

    private fun normalizePortalUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            clean = "http://$clean"
        }
        // Normalize portal path
        if (clean.endsWith("/c/") || clean.endsWith("/c")) {
            clean = clean.substringBeforeLast("/c")
        }
        return clean.trimEnd('/')
    }

    suspend fun connectAndFetchChannels(portalUrl: String, macAddress: String, profileId: Long): Pair<StalkerAuthResult, List<ChannelEntity>> = withContext(Dispatchers.IO) {
        val base = normalizePortalUrl(portalUrl)
        val mac = macAddress.trim().uppercase()
        val cookieHeader = "mac=$mac; stb_lang=en; timezone=GMT;"

        val loadPhpUrl = if (base.contains("server/load.php")) base else "$base/server/load.php"

        try {
            // Step 1: Handshake
            val handshakeUrl = "$loadPhpUrl?type=stb&action=handshake&token=&JsHttpRequest=1-xml"
            val handshakeReq = Request.Builder()
                .url(handshakeUrl)
                .header("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 2712 Mobile Safari/533.3")
                .header("Cookie", cookieHeader)
                .build()

            val handshakeRes = httpClient.newCall(handshakeReq).execute()
            val handshakeBody = handshakeRes.body?.string() ?: ""

            var token = ""
            if (handshakeBody.contains("\"token\"")) {
                val json = JSONObject(handshakeBody)
                token = json.optJSONObject("js")?.optString("token") ?: ""
            }

            // Step 2: Fetch genres
            val genresMap = mutableMapOf<String, String>()
            try {
                val genresUrl = "$loadPhpUrl?type=itv&action=get_genres&JsHttpRequest=1-xml"
                val genresReq = Request.Builder()
                    .url(genresUrl)
                    .header("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG250")
                    .header("Cookie", "$cookieHeader token=$token;")
                    .header("Authorization", "Bearer $token")
                    .build()

                val genresRes = httpClient.newCall(genresReq).execute()
                val genresBody = genresRes.body?.string() ?: ""
                if (genresBody.contains("\"js\"")) {
                    val genresJson = JSONObject(genresBody).optJSONObject("js")?.optJSONArray("data")
                    if (genresJson != null) {
                        for (i in 0 until genresJson.length()) {
                            val g = genresJson.getJSONObject(i)
                            genresMap[g.optString("id")] = g.optString("title")
                        }
                    }
                }
            } catch (_: Exception) {}

            // Step 3: Fetch all channels
            val channelsUrl = "$loadPhpUrl?type=itv&action=get_all_channels&JsHttpRequest=1-xml"
            val channelsReq = Request.Builder()
                .url(channelsUrl)
                .header("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG250")
                .header("Cookie", "$cookieHeader token=$token;")
                .header("Authorization", "Bearer $token")
                .build()

            val chRes = httpClient.newCall(channelsReq).execute()
            val chBody = chRes.body?.string() ?: throw Exception("Empty channels response from Stalker Portal")

            val channels = mutableListOf<ChannelEntity>()
            if (chBody.contains("\"data\"")) {
                val root = JSONObject(chBody)
                val data = root.optJSONObject("js")?.optJSONArray("data")
                if (data != null) {
                    for (i in 0 until data.length()) {
                        val item = data.getJSONObject(i)
                        val id = item.optString("id")
                        val name = item.optString("name", "Stalker Channel ${i + 1}")
                        val num = item.optInt("number", i + 1)
                        val genreId = item.optString("tv_genre_id")
                        val logo = item.optString("logo").takeIf { it.isNotBlank() }
                        var cmd = item.optString("cmd")

                        // Extract actual stream URL from Stalker cmd string (e.g., "ffrt http://..." or "auto /ch/...")
                        cmd = cmd.removePrefix("ffmpeg ").removePrefix("ffrt ").removePrefix("auto ").trim()
                        if (cmd.startsWith("/")) {
                            cmd = "$base$cmd"
                        }

                        if (cmd.startsWith("http://") || cmd.startsWith("https://")) {
                            channels.add(
                                ChannelEntity(
                                    id = "${profileId}_stalker_$id",
                                    profileId = profileId,
                                    name = name,
                                    streamUrl = cmd,
                                    logoUrl = logo,
                                    groupTitle = genresMap[genreId] ?: "Live Portal",
                                    tvgId = "stalker_$id",
                                    tvgName = name,
                                    channelNumber = num,
                                    streamFormat = "m3u8"
                                )
                            )
                        }
                    }
                }
            }

            Pair(
                StalkerAuthResult(true, token, "Connected successfully", channels.size),
                channels
            )
        } catch (e: Exception) {
            Pair(
                StalkerAuthResult(false, null, e.localizedMessage ?: "Failed to connect to Stalker Portal"),
                emptyList()
            )
        }
    }
}
