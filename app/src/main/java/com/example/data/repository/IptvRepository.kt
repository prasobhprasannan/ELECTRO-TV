package com.example.data.repository

import com.example.data.local.IptvDao
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelWithEpg
import com.example.data.model.EpgProgramEntity
import com.example.data.model.IptvType
import com.example.data.model.PlaylistProfileEntity
import com.example.data.model.WatchlistEntity
import com.example.data.network.M3uParser
import com.example.data.network.PublicDemoData
import com.example.data.network.StalkerPortalApi
import com.example.data.network.XmlTvParser
import com.example.data.network.XtreamCodesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class IptvRepository(private val dao: IptvDao) {

    val allProfiles: Flow<List<PlaylistProfileEntity>> = dao.getAllProfiles()
    val activeProfile: Flow<PlaylistProfileEntity?> = dao.getActiveProfile()
    val allWatchlists: Flow<List<WatchlistEntity>> = dao.getAllWatchlists()

    // Channels
    fun getChannelsForProfile(profileId: Long): Flow<List<ChannelEntity>> = dao.getChannelsForProfile(profileId)
    fun getAllChannels(): Flow<List<ChannelEntity>> = dao.getAllChannels()
    fun getFavoriteChannels(): Flow<List<ChannelEntity>> = dao.getFavoriteChannels()
    fun getRecentlyWatchedChannels(): Flow<List<ChannelEntity>> = dao.getRecentlyWatchedChannels()

    // Movies (VOD)
    fun getAllMovies(): Flow<List<com.example.data.model.MovieEntity>> = dao.getAllMovies()
    fun getMoviesForProfile(profileId: Long): Flow<List<com.example.data.model.MovieEntity>> = dao.getMoviesForProfile(profileId)
    fun getFavoriteMovies(): Flow<List<com.example.data.model.MovieEntity>> = dao.getFavoriteMovies()
    suspend fun toggleMovieFavorite(movieId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.setMovieFavorite(movieId, !currentStatus)
    }

    // Series (TV Shows)
    fun getAllSeries(): Flow<List<com.example.data.model.SeriesEntity>> = dao.getAllSeries()
    fun getSeriesForProfile(profileId: Long): Flow<List<com.example.data.model.SeriesEntity>> = dao.getSeriesForProfile(profileId)
    fun getFavoriteSeries(): Flow<List<com.example.data.model.SeriesEntity>> = dao.getFavoriteSeries()
    fun getEpisodesForSeries(seriesId: String): Flow<List<com.example.data.model.EpisodeEntity>> = dao.getEpisodesForSeries(seriesId)
    suspend fun toggleSeriesFavorite(seriesId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.setSeriesFavorite(seriesId, !currentStatus)
    }

    fun getProgramsForChannel(tvgId: String): Flow<List<EpgProgramEntity>> = dao.getProgramsForChannel(tvgId)
    fun getAllPrograms(): Flow<List<EpgProgramEntity>> = dao.getAllPrograms()

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        // Initialize default watchlist categories for user if none exist
        val watchlists = allWatchlists.firstOrNull() ?: emptyList()
        if (watchlists.isEmpty()) {
            dao.insertWatchlist(WatchlistEntity(name = "Favorites", colorHex = "#F59E0B", iconName = "star"))
            dao.insertWatchlist(WatchlistEntity(name = "Sports", colorHex = "#10B981", iconName = "sports"))
            dao.insertWatchlist(WatchlistEntity(name = "News", colorHex = "#3B82F6", iconName = "feed"))
            dao.insertWatchlist(WatchlistEntity(name = "Movies", colorHex = "#8B5CF6", iconName = "movie"))
        }

        // Ensure that if profiles exist, at least one is active so channels/movies/series load properly
        val allCurrentProfiles = dao.getAllProfiles().firstOrNull() ?: emptyList()
        val currentActive = dao.getActiveProfile().firstOrNull()
        if (allCurrentProfiles.isNotEmpty() && currentActive == null) {
            dao.setActiveProfile(allCurrentProfiles.first().id)
        }
    }

    suspend fun loadOpenSourcesPlaylist(): Long = withContext(Dispatchers.IO) {
        val profile = PublicDemoData.defaultDemoProfile
        dao.deactivateAllProfiles()
        val profileId = dao.insertProfile(profile.copy(id = 0, isActive = true))
        val channels = PublicDemoData.getDemoChannels(profileId)
        dao.insertChannels(channels)
        dao.updateProfileChannelCount(profileId, channels.size)

        val demoMovies = PublicDemoData.getDemoMovies(profileId)
        dao.insertMovies(demoMovies)

        val demoSeries = PublicDemoData.getDemoSeries(profileId)
        dao.insertSeries(demoSeries)

        val demoEpisodes = PublicDemoData.getDemoEpisodes(profileId)
        dao.insertEpisodes(demoEpisodes)

        val demoPrograms = PublicDemoData.getDemoEpgPrograms()
        dao.insertPrograms(demoPrograms)

        dao.setActiveProfile(profileId)
        profileId
    }

    suspend fun switchActiveProfile(profileId: Long) = withContext(Dispatchers.IO) {
        dao.deactivateAllProfiles()
        dao.setActiveProfile(profileId)
    }

    suspend fun deleteProfile(profileId: Long) = withContext(Dispatchers.IO) {
        dao.deleteChannelsForProfile(profileId)
        dao.deleteMoviesForProfile(profileId)
        dao.deleteSeriesForProfile(profileId)
        dao.deleteProfile(profileId)
        // If the deleted profile was active, set the first available profile as active
        val remaining = dao.getAllProfiles().firstOrNull() ?: emptyList()
        if (remaining.isNotEmpty()) {
            dao.setActiveProfile(remaining.first().id)
        } else {
            dao.clearAllChannels()
            dao.clearAllMovies()
            dao.clearAllSeries()
            dao.clearAllEpisodes()
            dao.clearAllEpg()
        }
    }

    suspend fun addXtreamProfile(
        name: String,
        serverUrl: String,
        username: String,
        password: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val auth = XtreamCodesApi.authenticate(serverUrl, username, password)
            if (!auth.success) {
                return@withContext Result.failure(Exception(auth.message ?: "Authentication failed"))
            }

            dao.deactivateAllProfiles()
            val profile = PlaylistProfileEntity(
                name = name.ifBlank { "Xtream (${auth.serverName ?: "Server"})" },
                type = IptvType.XTREAM.name,
                serverUrl = serverUrl,
                username = username,
                password = password,
                isActive = true
            )
            val profileId = dao.insertProfile(profile)
            dao.setActiveProfile(profileId)

            val channels = XtreamCodesApi.fetchChannels(serverUrl, username, password, profileId)
            dao.insertChannels(channels)
            dao.updateProfileChannelCount(profileId, channels.size)

            val vodMovies = XtreamCodesApi.fetchVodMovies(serverUrl, username, password, profileId)
            if (vodMovies.isNotEmpty()) {
                dao.insertMovies(vodMovies)
            }

            val series = XtreamCodesApi.fetchSeries(serverUrl, username, password, profileId)
            if (series.isNotEmpty()) {
                dao.insertSeries(series)
            }

            Result.success(profileId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addM3uProfile(
        name: String,
        urlOrContent: String,
        isRawContent: Boolean = false,
        epgUrl: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            dao.deactivateAllProfiles()
            val profile = PlaylistProfileEntity(
                name = name.ifBlank { "M3U Playlist" },
                type = IptvType.M3U.name,
                serverUrl = if (isRawContent) "Local M3U" else urlOrContent,
                epgUrl = epgUrl,
                isActive = true
            )
            val profileId = dao.insertProfile(profile)
            dao.setActiveProfile(profileId)

            val channels = if (isRawContent) {
                M3uParser.parseContent(urlOrContent, profileId)
            } else {
                M3uParser.parseFromUrl(urlOrContent, profileId)
            }

            if (channels.isEmpty()) {
                dao.deleteProfile(profileId)
                return@withContext Result.failure(Exception("No playable streams found in playlist"))
            }

            dao.insertChannels(channels)
            dao.updateProfileChannelCount(profileId, channels.size)

            // Extract movies & series from M3U if present
            val m3uMovies = channels.filter { ch ->
                val grp = ch.groupTitle.lowercase()
                grp.contains("movie") || grp.contains("cinema") || grp.contains("film") || grp.contains("vod") ||
                ch.streamUrl.endsWith(".mp4", ignoreCase = true) || ch.streamUrl.endsWith(".mkv", ignoreCase = true)
            }.map { ch ->
                com.example.data.model.MovieEntity(
                    id = "mov_${ch.id}",
                    profileId = profileId,
                    title = ch.name,
                    streamUrl = ch.streamUrl,
                    posterUrl = ch.logoUrl,
                    genre = if (ch.groupTitle.isNotBlank()) ch.groupTitle else "Movie",
                    containerExtension = if (ch.streamUrl.contains(".mp4")) "mp4" else if (ch.streamUrl.contains(".mkv")) "mkv" else "m3u8"
                )
            }
            if (m3uMovies.isNotEmpty()) {
                dao.insertMovies(m3uMovies)
            }

            val m3uSeries = channels.filter { ch ->
                val grp = ch.groupTitle.lowercase()
                grp.contains("series") || grp.contains("show") || grp.contains("tv show") || grp.contains("season")
            }.groupBy { it.groupTitle }.map { (genre, chs) ->
                val first = chs.first()
                com.example.data.model.SeriesEntity(
                    id = "ser_${first.id}",
                    profileId = profileId,
                    title = first.name.substringBefore("S0").substringBefore("E0").ifBlank { first.name },
                    genre = genre,
                    posterUrl = first.logoUrl
                )
            }
            if (m3uSeries.isNotEmpty()) {
                dao.insertSeries(m3uSeries)
            }

            // Parse EPG if provided
            if (epgUrl.isNotBlank()) {
                try {
                    val epgPrograms = XmlTvParser.parseFromUrl(epgUrl)
                    dao.insertPrograms(epgPrograms)
                } catch (_: Exception) {}
            }

            Result.success(profileId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStalkerProfile(
        name: String,
        portalUrl: String,
        macAddress: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val (authResult, channels) = StalkerPortalApi.connectAndFetchChannels(portalUrl, macAddress, 0L)
            if (!authResult.success) {
                return@withContext Result.failure(Exception(authResult.message ?: "Failed to connect to Stalker portal"))
            }

            dao.deactivateAllProfiles()
            val profile = PlaylistProfileEntity(
                name = name.ifBlank { "MAG Portal" },
                type = IptvType.STALKER.name,
                serverUrl = portalUrl,
                macAddress = macAddress,
                isActive = true
            )
            val profileId = dao.insertProfile(profile)
            dao.setActiveProfile(profileId)

            // Re-assign correct profileId
            val remappedChannels = channels.map { ch ->
                ch.copy(
                    id = "${profileId}_${ch.id.substringAfter('_')}",
                    profileId = profileId
                )
            }

            dao.insertChannels(remappedChannels)
            dao.updateProfileChannelCount(profileId, remappedChannels.size)

            Result.success(profileId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(channelId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(channelId, !currentStatus)
    }

    suspend fun recordChannelWatched(channelId: String) = withContext(Dispatchers.IO) {
        dao.updateLastWatched(channelId, System.currentTimeMillis())
    }

    suspend fun toggleWatchlistMembership(channelId: String, watchlistName: String) = withContext(Dispatchers.IO) {
        val channel = dao.getChannelById(channelId) ?: return@withContext
        val list = channel.watchlistNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        if (list.contains(watchlistName)) {
            list.remove(watchlistName)
        } else {
            list.add(watchlistName)
        }
        val updatedString = list.joinToString(",")
        dao.updateWatchlists(channelId, updatedString)
    }

    suspend fun createWatchlist(name: String, colorHex: String = "#38BDF8"): Long = withContext(Dispatchers.IO) {
        dao.insertWatchlist(WatchlistEntity(name = name.trim(), colorHex = colorHex))
    }

    suspend fun deleteWatchlist(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteWatchlist(id)
    }

    suspend fun syncEpgForChannel(channel: ChannelEntity, activeProfile: PlaylistProfileEntity?) = withContext(Dispatchers.IO) {
        if (activeProfile?.type == IptvType.XTREAM.name && channel.streamUrl.contains("/live/")) {
            val streamId = channel.streamUrl.substringAfterLast("/").substringBefore(".").toIntOrNull()
            if (streamId != null && !channel.tvgId.isNullOrEmpty()) {
                val programs = XtreamCodesApi.fetchChannelEpg(
                    activeProfile.serverUrl,
                    activeProfile.username,
                    activeProfile.password,
                    streamId,
                    channel.tvgId
                )
                if (programs.isNotEmpty()) {
                    dao.insertPrograms(programs)
                }
            }
        }
    }

    suspend fun generateFallbackEpgIfMissing(channel: ChannelEntity): Pair<EpgProgramEntity, EpgProgramEntity> {
        val now = System.currentTimeMillis()
        val hour = 3600_000L
        val slotStart = (now / hour) * hour
        val currentSlotEnd = slotStart + hour

        val title = when (channel.groupTitle.lowercase()) {
            "sports", "portal: sports" -> "Live Match & Championship Coverage"
            "news", "portal: news" -> "Global News Bulletin Live"
            "movies & cinema", "movies", "portal: movies" -> "Featured Primetime Cinema"
            "science & tech" -> "Deep Space & Tech Explorations"
            "music" -> "Non-Stop High Fidelity Live Sessions"
            "documentary" -> "Planet Earth & Ocean Odyssey"
            else -> "${channel.name} Broadcast"
        }

        val nextTitle = when (channel.groupTitle.lowercase()) {
            "sports", "portal: sports" -> "Post-Match Analysis & Highlights"
            "news", "portal: news" -> "World Weather & Financial Wrap"
            "movies & cinema", "movies", "portal: movies" -> "Late Night Feature Film"
            "science & tech" -> "Cosmos & Engineering Frontiers"
            "music" -> "Night Lounge Ambient Beats"
            "documentary" -> "Ancient Civilizations Survey"
            else -> "Scheduled Programming"
        }

        val cur = EpgProgramEntity(
            id = "${channel.tvgId ?: channel.id}_$slotStart",
            channelTvgId = channel.tvgId ?: channel.id,
            title = title,
            description = "Currently streaming live in high definition on ${channel.name}.",
            startTimeEpoch = slotStart,
            endTimeEpoch = currentSlotEnd,
            category = channel.groupTitle
        )

        val next = EpgProgramEntity(
            id = "${channel.tvgId ?: channel.id}_$currentSlotEnd",
            channelTvgId = channel.tvgId ?: channel.id,
            title = nextTitle,
            description = "Upcoming segment following the current live broadcast.",
            startTimeEpoch = currentSlotEnd,
            endTimeEpoch = currentSlotEnd + hour,
            category = channel.groupTitle
        )

        return Pair(cur, next)
    }
}
