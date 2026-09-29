package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class IptvType {
    XTREAM,
    M3U,
    STALKER,
    DEMO
}

@Entity(tableName = "playlist_profiles")
data class PlaylistProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // XTREAM, M3U, STALKER, DEMO
    val serverUrl: String,
    val username: String = "",
    val password: String = "",
    val macAddress: String = "",
    val epgUrl: String = "",
    val isActive: Boolean = false,
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
    val id: String,
    val profileId: Long,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "General",
    val tvgId: String? = null,
    val tvgName: String? = null,
    val channelNumber: Int = 1,
    val isFavorite: Boolean = false,
    val watchlistNames: String = "", // comma-separated watchlist names e.g. "Sports,Favorites"
    val lastWatchedTimestamp: Long = 0,
    val streamFormat: String = "m3u8"
)

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val profileId: Long,
    val title: String,
    val streamUrl: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val genre: String = "Action",
    val rating: String = "8.5",
    val releaseYear: String = "2024",
    val duration: String = "1h 45m",
    val plot: String = "",
    val cast: String = "",
    val isFavorite: Boolean = false,
    val containerExtension: String = "mp4"
) {
    fun toPlayableChannel(): ChannelEntity = ChannelEntity(
        id = "vod_$id",
        profileId = profileId,
        name = title,
        streamUrl = streamUrl,
        logoUrl = posterUrl ?: backdropUrl,
        groupTitle = genre,
        channelNumber = 0,
        streamFormat = containerExtension
    )
}

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val profileId: Long,
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val genre: String = "Sci-Fi",
    val rating: String = "8.8",
    val releaseYear: String = "2023",
    val plot: String = "",
    val cast: String = "",
    val seasonsCount: Int = 1,
    val isFavorite: Boolean = false
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val streamUrl: String,
    val thumbnail: String? = null,
    val plot: String = "",
    val duration: String = "45m"
) {
    fun toPlayableChannel(series: SeriesEntity): ChannelEntity = ChannelEntity(
        id = "ep_${series.id}_${seasonNumber}_${episodeNumber}",
        profileId = series.profileId,
        name = "${series.title} • S${seasonNumber}E${episodeNumber} $title",
        streamUrl = streamUrl,
        logoUrl = thumbnail ?: series.posterUrl,
        groupTitle = series.genre,
        channelNumber = episodeNumber,
        streamFormat = "mp4"
    )
}

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey
    val id: String,
    val channelTvgId: String,
    val title: String,
    val description: String = "",
    val startTimeEpoch: Long,
    val endTimeEpoch: Long,
    val category: String = ""
)

@Entity(tableName = "watchlists")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#38BDF8",
    val iconName: String = "bookmark",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChannelWithEpg(
    val channel: ChannelEntity,
    val currentProgram: EpgProgramEntity? = null,
    val nextProgram: EpgProgramEntity? = null
)
