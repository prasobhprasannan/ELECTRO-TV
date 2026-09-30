package com.example.data.network

import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class XtreamAuthResult(
    val success: Boolean,
    val status: String,
    val message: String? = null,
    val expDate: String? = null,
    val maxConnections: String? = null,
    val serverName: String? = null
)

object XtreamCodesApi {

    private val httpClient: OkHttpClient
        get() = try {
            AppNetworkClient.getOkHttpClient(com.example.IptvApplication.instance)
        } catch (_: Exception) {
            defaultHttpClient
        }

    private val defaultHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private fun normalizeServerUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            clean = "http://$clean"
        }
        return clean.trimEnd('/')
    }

    suspend fun authenticate(serverUrl: String, user: String, pass: String): XtreamAuthResult = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)
        val endpoint = "$base/player_api.php?username=$user&password=$pass"

        val request = Request.Builder()
            .url(endpoint)
            .header("User-Agent", "IPTVSmarters/1.0 (Linux;Android 14)")
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response from server")
            val json = JSONObject(body)

            if (!json.has("user_info")) {
                return@withContext XtreamAuthResult(false, "Unknown", "Invalid server response structure")
            }

            val userInfo = json.getJSONObject("user_info")
            val auth = userInfo.optInt("auth", 0)
            val status = userInfo.optString("status", "Unknown")

            if (auth == 1 || status.equals("Active", ignoreCase = true)) {
                val expDate = userInfo.optString("exp_date", "Unlimited")
                val maxConnections = userInfo.optString("max_connections", "1")
                val serverInfo = json.optJSONObject("server_info")
                val serverName = serverInfo?.optString("server_protocol", "http") ?: "Xtream"

                XtreamAuthResult(
                    success = true,
                    status = status,
                    expDate = expDate,
                    maxConnections = maxConnections,
                    serverName = serverName
                )
            } else {
                val message = userInfo.optString("message", "Authentication failed: Status is $status")
                XtreamAuthResult(false, status, message)
            }
        } catch (e: Exception) {
            XtreamAuthResult(false, "Error", e.localizedMessage ?: "Failed to connect to Xtream server")
        }
    }

    suspend fun fetchChannels(serverUrl: String, user: String, pass: String, profileId: Long): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)

        // Fetch categories first to map category_id -> category_name
        val categoriesMap = mutableMapOf<String, String>()
        try {
            val catUrl = "$base/player_api.php?username=$user&password=$pass&action=get_live_categories"
            val catReq = Request.Builder().url(catUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val catRes = httpClient.newCall(catReq).execute()
            val catBody = catRes.body?.string()
            if (!catBody.isNullOrEmpty() && catBody.startsWith("[")) {
                val catArray = JSONArray(catBody)
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val id = obj.optString("category_id")
                    val name = obj.optString("category_name")
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        categoriesMap[id] = name
                    }
                }
            }
        } catch (_: Exception) {
            // Non-critical, fallback to category_id
        }

        // Fetch live streams
        val streamsUrl = "$base/player_api.php?username=$user&password=$pass&action=get_live_streams"
        val streamsReq = Request.Builder().url(streamsUrl).header("User-Agent", "IPTVSmarters/1.0").build()
        val streamsRes = httpClient.newCall(streamsReq).execute()
        val streamsBody = streamsRes.body?.string() ?: throw Exception("Failed to retrieve live streams")

        if (!streamsBody.startsWith("[")) {
            throw Exception("Invalid streams payload: $streamsBody")
        }

        val array = JSONArray(streamsBody)
        val channels = mutableListOf<ChannelEntity>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val streamId = item.optInt("stream_id")
            val name = item.optString("name", "Channel ${i + 1}")
            val icon = item.optString("stream_icon").takeIf { it.isNotBlank() }
            val catId = item.optString("category_id")
            val catName = categoriesMap[catId] ?: "General"
            val epgId = item.optString("epg_channel_id").takeIf { it.isNotBlank() }
            val num = item.optInt("num", i + 1)

            // Live stream URL: {server}/live/{user}/{pass}/{streamId}.m3u8
            val streamUrl = "$base/live/$user/$pass/$streamId.m3u8"

            channels.add(
                ChannelEntity(
                    id = "${profileId}_${streamId}",
                    profileId = profileId,
                    name = name,
                    streamUrl = streamUrl,
                    logoUrl = icon,
                    groupTitle = catName,
                    tvgId = epgId ?: "xtream_$streamId",
                    tvgName = name,
                    channelNumber = num,
                    streamFormat = "m3u8"
                )
            )
        }

        channels
    }

    suspend fun fetchChannelEpg(serverUrl: String, user: String, pass: String, streamId: Int, tvgId: String): List<EpgProgramEntity> = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)
        val epgUrl = "$base/player_api.php?username=$user&password=$pass&action=get_simple_data_table&stream_id=$streamId"
        val req = Request.Builder().url(epgUrl).header("User-Agent", "IPTVSmarters/1.0").build()

        val list = mutableListOf<EpgProgramEntity>()
        try {
            val res = httpClient.newCall(req).execute()
            val body = res.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val listings = json.optJSONArray("epg_listings") ?: return@withContext emptyList()

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            for (i in 0 until listings.length()) {
                val item = listings.getJSONObject(i)
                val title = item.optString("title")
                val desc = item.optString("description")
                val startStr = item.optString("start")
                val endStr = item.optString("end")

                val startTime = try { sdf.parse(startStr)?.time ?: 0L } catch (_: Exception) { 0L }
                val endTime = try { sdf.parse(endStr)?.time ?: 0L } catch (_: Exception) { 0L }

                if (startTime > 0 && endTime > startTime) {
                    list.add(
                        EpgProgramEntity(
                            id = "${tvgId}_${startTime}",
                            channelTvgId = tvgId,
                            title = title,
                            description = desc,
                            startTimeEpoch = startTime,
                            endTimeEpoch = endTime
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        list
    }

    suspend fun fetchVodMovies(serverUrl: String, user: String, pass: String, profileId: Long): List<com.example.data.model.MovieEntity> = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)
        val movies = mutableListOf<com.example.data.model.MovieEntity>()

        // 1. Fetch VOD categories
        val catMap = mutableMapOf<String, String>()
        try {
            val catUrl = "$base/player_api.php?username=$user&password=$pass&action=get_vod_categories"
            val catReq = Request.Builder().url(catUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val catRes = httpClient.newCall(catReq).execute()
            val catBody = catRes.body?.string()
            if (!catBody.isNullOrEmpty() && catBody.startsWith("[")) {
                val arr = JSONArray(catBody)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    catMap[obj.optString("category_id")] = obj.optString("category_name")
                }
            }
        } catch (_: Exception) {}

        // 2. Fetch VOD streams
        try {
            val vodUrl = "$base/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val vodReq = Request.Builder().url(vodUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val vodRes = httpClient.newCall(vodReq).execute()
            val vodBody = vodRes.body?.string() ?: return@withContext emptyList()
            if (!vodBody.startsWith("[")) return@withContext emptyList()

            val arr = JSONArray(vodBody)
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val streamId = item.optInt("stream_id")
                val name = item.optString("name", "Movie")
                val icon = item.optString("stream_icon").takeIf { it.isNotBlank() }
                val catId = item.optString("category_id")
                val genre = catMap[catId] ?: "Movies"
                val ext = item.optString("container_extension", "mp4").ifEmpty { "mp4" }
                val rating = item.optString("rating_5based", item.optString("rating", "8.0"))

                // VOD Movie stream URL: {server}/movie/{user}/{pass}/{stream_id}.{ext}
                val streamUrl = "$base/movie/$user/$pass/$streamId.$ext"

                movies.add(
                    com.example.data.model.MovieEntity(
                        id = "${profileId}_vod_$streamId",
                        profileId = profileId,
                        title = name,
                        streamUrl = streamUrl,
                        posterUrl = icon,
                        backdropUrl = icon,
                        genre = genre,
                        rating = rating,
                        releaseYear = "2023",
                        duration = "1h 50m",
                        plot = "On-demand movie from $genre stream catalog.",
                        containerExtension = ext
                    )
                )
            }
        } catch (_: Exception) {}

        movies
    }

    suspend fun fetchSeries(serverUrl: String, user: String, pass: String, profileId: Long): List<com.example.data.model.SeriesEntity> = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)
        val seriesList = mutableListOf<com.example.data.model.SeriesEntity>()

        // 1. Fetch Series categories
        val catMap = mutableMapOf<String, String>()
        try {
            val catUrl = "$base/player_api.php?username=$user&password=$pass&action=get_series_categories"
            val catReq = Request.Builder().url(catUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val catRes = httpClient.newCall(catReq).execute()
            val catBody = catRes.body?.string()
            if (!catBody.isNullOrEmpty() && catBody.startsWith("[")) {
                val arr = JSONArray(catBody)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    catMap[obj.optString("category_id")] = obj.optString("category_name")
                }
            }
        } catch (_: Exception) {}

        // 2. Fetch Series
        try {
            val seriesUrl = "$base/player_api.php?username=$user&password=$pass&action=get_series"
            val seriesReq = Request.Builder().url(seriesUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val seriesRes = httpClient.newCall(seriesReq).execute()
            val seriesBody = seriesRes.body?.string() ?: return@withContext emptyList()
            if (!seriesBody.startsWith("[")) return@withContext emptyList()

            val arr = JSONArray(seriesBody)
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val seriesId = item.optInt("series_id")
                val name = item.optString("name", "Series")
                val cover = item.optString("cover").takeIf { it.isNotBlank() }
                val backdrop = item.optJSONArray("backdrop_path")?.optString(0) ?: cover
                val catId = item.optString("category_id")
                val genre = catMap[catId] ?: item.optString("genre", "Series")
                val plot = item.optString("plot", "Complete TV series on-demand.")
                val cast = item.optString("cast", "")
                val rating = item.optString("rating_5based", item.optString("rating", "8.5"))

                seriesList.add(
                    com.example.data.model.SeriesEntity(
                        id = "${profileId}_series_$seriesId",
                        profileId = profileId,
                        title = name,
                        posterUrl = cover,
                        backdropUrl = backdrop,
                        genre = genre,
                        rating = rating,
                        plot = plot,
                        cast = cast,
                        seasonsCount = 1
                    )
                )
            }
        } catch (_: Exception) {}

        seriesList
    }

    suspend fun fetchSeriesEpisodes(serverUrl: String, user: String, pass: String, seriesDbId: String, seriesXtreamId: Int): List<com.example.data.model.EpisodeEntity> = withContext(Dispatchers.IO) {
        val base = normalizeServerUrl(serverUrl)
        val episodes = mutableListOf<com.example.data.model.EpisodeEntity>()

        try {
            val infoUrl = "$base/player_api.php?username=$user&password=$pass&action=get_series_info&series_id=$seriesXtreamId"
            val req = Request.Builder().url(infoUrl).header("User-Agent", "IPTVSmarters/1.0").build()
            val res = httpClient.newCall(req).execute()
            val body = res.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)

            val episodesObj = json.optJSONObject("episodes") ?: return@withContext emptyList()
            val seasonKeys = episodesObj.keys()
            while (seasonKeys.hasNext()) {
                val sKey = seasonKeys.next()
                val sNum = sKey.toIntOrNull() ?: 1
                val epArr = episodesObj.optJSONArray(sKey) ?: continue
                for (j in 0 until epArr.length()) {
                    val epItem = epArr.getJSONObject(j)
                    val epId = epItem.optString("id")
                    val epNum = epItem.optInt("episode_num", j + 1)
                    val title = epItem.optString("title", "Episode $epNum")
                    val ext = epItem.optString("container_extension", "mp4").ifEmpty { "mp4" }
                    val info = epItem.optJSONObject("info")
                    val plot = info?.optString("plot") ?: ""
                    val duration = info?.optString("duration") ?: "45m"
                    val thumb = info?.optString("movie_image")

                    // Series episode stream URL: {server}/series/{user}/{pass}/{epId}.{ext}
                    val streamUrl = "$base/series/$user/$pass/$epId.$ext"

                    episodes.add(
                        com.example.data.model.EpisodeEntity(
                            id = "xt_ep_${seriesDbId}_${epId}",
                            seriesId = seriesDbId,
                            seasonNumber = sNum,
                            episodeNumber = epNum,
                            title = title,
                            streamUrl = streamUrl,
                            thumbnail = thumb,
                            plot = plot,
                            duration = duration
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        episodes
    }
}
