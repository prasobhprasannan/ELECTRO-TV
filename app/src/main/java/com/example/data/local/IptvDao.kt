package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChannelEntity
import com.example.data.model.EpisodeEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.MovieEntity
import com.example.data.model.PlaylistProfileEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {

    // Profiles
    @Query("SELECT * FROM playlist_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<PlaylistProfileEntity>>

    @Query("SELECT * FROM playlist_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<PlaylistProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlaylistProfileEntity): Long

    @Query("UPDATE playlist_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE playlist_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun setActiveProfile(profileId: Long)

    @Query("DELETE FROM playlist_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)

    @Query("UPDATE playlist_profiles SET channelCount = :count WHERE id = :profileId")
    suspend fun updateProfileChannelCount(profileId: Long, count: Int)

    // Channels
    @Query("SELECT * FROM channels WHERE profileId = :profileId ORDER BY channelNumber ASC, name ASC")
    fun getChannelsForProfile(profileId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels ORDER BY channelNumber ASC, name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE lastWatchedTimestamp > 0 ORDER BY lastWatchedTimestamp DESC LIMIT 30")
    fun getRecentlyWatchedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun setFavorite(channelId: String, isFavorite: Boolean)

    @Query("UPDATE channels SET lastWatchedTimestamp = :timestamp WHERE id = :channelId")
    suspend fun updateLastWatched(channelId: String, timestamp: Long)

    @Query("UPDATE channels SET watchlistNames = :watchlists WHERE id = :channelId")
    suspend fun updateWatchlists(channelId: String, watchlists: String)

    @Query("DELETE FROM channels WHERE profileId = :profileId")
    suspend fun deleteChannelsForProfile(profileId: Long)

    // Movies (VOD)
    @Query("SELECT * FROM movies ORDER BY releaseYear DESC, title ASC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE profileId = :profileId ORDER BY releaseYear DESC, title ASC")
    fun getMoviesForProfile(profileId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: String): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun setMovieFavorite(movieId: String, isFavorite: Boolean)

    @Query("DELETE FROM movies WHERE profileId = :profileId")
    suspend fun deleteMoviesForProfile(profileId: Long)

    // Series (TV Shows)
    @Query("SELECT * FROM series ORDER BY rating DESC, title ASC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE profileId = :profileId ORDER BY rating DESC, title ASC")
    fun getSeriesForProfile(profileId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :seriesId LIMIT 1")
    suspend fun getSeriesById(seriesId: String): SeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<SeriesEntity>)

    @Query("UPDATE series SET isFavorite = :isFavorite WHERE id = :seriesId")
    suspend fun setSeriesFavorite(seriesId: String, isFavorite: Boolean)

    @Query("DELETE FROM series WHERE profileId = :profileId")
    suspend fun deleteSeriesForProfile(profileId: Long)

    // Episodes
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)

    // EPG
    @Query("SELECT * FROM epg_programs WHERE channelTvgId = :channelTvgId ORDER BY startTimeEpoch ASC")
    fun getProgramsForChannel(channelTvgId: String): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE startTimeEpoch <= :currentTime AND endTimeEpoch >= :currentTime")
    fun getCurrentPrograms(currentTime: Long): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs ORDER BY startTimeEpoch ASC")
    fun getAllPrograms(): Flow<List<EpgProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE endTimeEpoch < :cutoffTime")
    suspend fun cleanOldEpg(cutoffTime: Long)

    @Query("DELETE FROM epg_programs")
    suspend fun clearAllEpg()

    // Watchlists
    @Query("SELECT * FROM watchlists ORDER BY createdAt ASC")
    fun getAllWatchlists(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(watchlist: WatchlistEntity): Long

    @Query("DELETE FROM watchlists WHERE id = :id")
    suspend fun deleteWatchlist(id: Long)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()

    @Query("DELETE FROM movies")
    suspend fun clearAllMovies()

    @Query("DELETE FROM series")
    suspend fun clearAllSeries()

    @Query("DELETE FROM episodes")
    suspend fun clearAllEpisodes()

    @Query("DELETE FROM playlist_profiles")
    suspend fun clearAllProfiles()
}
