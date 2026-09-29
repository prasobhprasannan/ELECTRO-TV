package com.example.data.network

import com.example.data.model.ChannelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object M3uParser {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val TVG_ID_REGEX = Pattern.compile("tvg-id=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val TVG_NAME_REGEX = Pattern.compile("tvg-name=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val TVG_LOGO_REGEX = Pattern.compile("tvg-logo=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val GROUP_TITLE_REGEX = Pattern.compile("group-title=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)

    suspend fun parseFromUrl(url: String, profileId: Long): List<ChannelEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) VLC/3.0.18")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download playlist (HTTP ${response.code})")
        }

        val content = response.body?.string() ?: throw Exception("Empty playlist response")
        parseContent(content, profileId)
    }

    fun parseContent(content: String, profileId: Long): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val lines = content.lines()

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentLogo: String? = null
        var currentGroup = "General"
        var currentName: String? = null
        var channelIndex = 1

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF", ignoreCase = true)) {
                // Parse attributes
                val tvgIdMatcher = TVG_ID_REGEX.matcher(line)
                currentTvgId = if (tvgIdMatcher.find()) tvgIdMatcher.group(1)?.trim() else null

                val tvgNameMatcher = TVG_NAME_REGEX.matcher(line)
                currentTvgName = if (tvgNameMatcher.find()) tvgNameMatcher.group(1)?.trim() else null

                val logoMatcher = TVG_LOGO_REGEX.matcher(line)
                currentLogo = if (logoMatcher.find()) logoMatcher.group(1)?.trim() else null

                val groupMatcher = GROUP_TITLE_REGEX.matcher(line)
                currentGroup = if (groupMatcher.find()) {
                    val group = groupMatcher.group(1)?.trim()
                    if (!group.isNullOrEmpty()) group else "General"
                } else "General"

                // Extract channel name after last comma
                val commaIndex = line.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                    line.substring(commaIndex + 1).trim()
                } else {
                    currentTvgName ?: "Channel $channelIndex"
                }
            } else if (line.startsWith("#EXTGRP:", ignoreCase = true)) {
                val group = line.substringAfter("#EXTGRP:").trim()
                if (group.isNotEmpty()) currentGroup = group
            } else if (!line.startsWith("#")) {
                // This is a stream URL
                if (line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true)) {
                    val name = currentName ?: currentTvgName ?: "Channel $channelIndex"
                    val tvgId = currentTvgId ?: "ch_${profileId}_$channelIndex"
                    val format = when {
                        line.contains(".m3u8", ignoreCase = true) -> "m3u8"
                        line.contains(".ts", ignoreCase = true) -> "ts"
                        line.contains(".mp4", ignoreCase = true) -> "mp4"
                        else -> "m3u8"
                    }

                    channels.add(
                        ChannelEntity(
                            id = "${profileId}_${channelIndex}_${tvgId.hashCode()}",
                            profileId = profileId,
                            name = name,
                            streamUrl = line,
                            logoUrl = currentLogo?.takeIf { it.isNotEmpty() },
                            groupTitle = currentGroup,
                            tvgId = tvgId,
                            tvgName = currentTvgName,
                            channelNumber = channelIndex,
                            streamFormat = format
                        )
                    )
                    channelIndex++
                }

                // Reset per-channel state
                currentTvgId = null
                currentTvgName = null
                currentLogo = null
                currentGroup = "General"
                currentName = null
            }
        }

        return channels
    }
}
