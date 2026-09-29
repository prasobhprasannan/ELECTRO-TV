package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.PlaylistProfileEntity
import com.example.data.model.WatchlistEntity
import com.example.data.repository.IptvRepository
import com.example.player.AspectRatioMode
import com.example.player.IptvPlayerManager
import com.example.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddProfileUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = IptvRepository(database.iptvDao())
    val playerManager = IptvPlayerManager(application)

    val playerUiState: StateFlow<PlayerUiState> = playerManager.uiState

    val profiles: StateFlow<List<PlaylistProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<PlaylistProfileEntity?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val watchlists: StateFlow<List<WatchlistEntity>> = repository.allWatchlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<ChannelEntity>> = repository.getFavoriteChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<ChannelEntity>> = repository.getRecentlyWatchedChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allChannels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val allChannels: StateFlow<List<ChannelEntity>> = _allChannels.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _addProfileState = MutableStateFlow(AddProfileUiState())
    val addProfileState: StateFlow<AddProfileUiState> = _addProfileState.asStateFlow()

    private val _epgMap = MutableStateFlow<Map<String, Pair<EpgProgramEntity?, EpgProgramEntity?>>>(emptyMap())
    val epgMap: StateFlow<Map<String, Pair<EpgProgramEntity?, EpgProgramEntity?>>> = _epgMap.asStateFlow()

    // Movies (VOD) - scoped to active profile
    val allMovies: StateFlow<List<com.example.data.model.MovieEntity>> = repository.activeProfile.flatMapLatest { profile ->
        if (profile != null) repository.getMoviesForProfile(profile.id) else repository.getAllMovies()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMovies: StateFlow<List<com.example.data.model.MovieEntity>> = repository.getFavoriteMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMovieGenre = MutableStateFlow("All")
    val selectedMovieGenre: StateFlow<String> = _selectedMovieGenre.asStateFlow()

    private val _movieSearchQuery = MutableStateFlow("")
    val movieSearchQuery: StateFlow<String> = _movieSearchQuery.asStateFlow()

    val filteredMovies: StateFlow<List<com.example.data.model.MovieEntity>> = combine(
        allMovies,
        _selectedMovieGenre,
        _movieSearchQuery
    ) { movies, genre, query ->
        movies.filter { movie ->
            val matchesGenre = when (genre) {
                "All" -> true
                "Favorites" -> movie.isFavorite
                else -> movie.genre.equals(genre, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                movie.title.contains(query, ignoreCase = true) ||
                movie.genre.contains(query, ignoreCase = true) ||
                movie.cast.contains(query, ignoreCase = true)
            matchesGenre && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Series (TV Shows) - scoped to active profile
    val allSeries: StateFlow<List<com.example.data.model.SeriesEntity>> = repository.activeProfile.flatMapLatest { profile ->
        if (profile != null) repository.getSeriesForProfile(profile.id) else repository.getAllSeries()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSeries: StateFlow<List<com.example.data.model.SeriesEntity>> = repository.getFavoriteSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSeriesGenre = MutableStateFlow("All")
    val selectedSeriesGenre: StateFlow<String> = _selectedSeriesGenre.asStateFlow()

    private val _seriesSearchQuery = MutableStateFlow("")
    val seriesSearchQuery: StateFlow<String> = _seriesSearchQuery.asStateFlow()

    val filteredSeries: StateFlow<List<com.example.data.model.SeriesEntity>> = combine(
        allSeries,
        _selectedSeriesGenre,
        _seriesSearchQuery
    ) { seriesList, genre, query ->
        seriesList.filter { series ->
            val matchesGenre = when (genre) {
                "All" -> true
                "Favorites" -> series.isFavorite
                else -> series.genre.equals(genre, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                series.title.contains(query, ignoreCase = true) ||
                series.genre.contains(query, ignoreCase = true) ||
                series.plot.contains(query, ignoreCase = true)
            matchesGenre && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered channels combining active list, category, and search query
    val filteredChannels: StateFlow<List<ChannelEntity>> = combine(
        _allChannels,
        _selectedCategory,
        _searchQuery
    ) { channels, category, query ->
        channels.filter { channel ->
            val matchesCategory = when (category) {
                "All" -> true
                "Favorites" -> channel.isFavorite
                else -> channel.groupTitle.equals(category, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                channel.name.contains(query, ignoreCase = true) ||
                channel.groupTitle.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }

        // Observe active profile and load its channels dynamically
        viewModelScope.launch {
            repository.activeProfile
                .flatMapLatest { profile ->
                    if (profile != null) {
                        repository.getChannelsForProfile(profile.id)
                    } else {
                        repository.getAllChannels()
                    }
                }
                .collect { chList ->
                    _allChannels.value = chList
                    refreshEpgForChannels(chList)
                }
        }
    }

    private fun refreshEpgForChannels(channels: List<ChannelEntity>) {
        viewModelScope.launch {
            val map = mutableMapOf<String, Pair<EpgProgramEntity?, EpgProgramEntity?>>()
            for (ch in channels) {
                val key = ch.tvgId ?: ch.id
                val (cur, next) = repository.generateFallbackEpgIfMissing(ch)
                map[key] = Pair(cur, next)
            }
            _epgMap.value = map
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun playChannel(channel: ChannelEntity) {
        playerManager.playChannel(channel)
        viewModelScope.launch {
            repository.recordChannelWatched(channel.id)
            repository.syncEpgForChannel(channel, activeProfile.value)
        }
    }

    fun zapNextChannel() {
        val current = playerUiState.value.currentChannel ?: return
        val list = filteredChannels.value.ifEmpty { allChannels.value }
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % list.size else 0
        playChannel(list[nextIndex])
    }

    fun zapPreviousChannel() {
        val current = playerUiState.value.currentChannel ?: return
        val list = filteredChannels.value.ifEmpty { allChannels.value }
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        playChannel(list[prevIndex])
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id, channel.isFavorite)
        }
    }

    // Movies Actions
    fun selectMovieGenre(genre: String) {
        _selectedMovieGenre.value = genre
    }

    fun setMovieSearchQuery(query: String) {
        _movieSearchQuery.value = query
    }

    fun playMovie(movie: com.example.data.model.MovieEntity) {
        val playable = movie.toPlayableChannel()
        playChannel(playable)
    }

    fun toggleMovieFavorite(movie: com.example.data.model.MovieEntity) {
        viewModelScope.launch {
            repository.toggleMovieFavorite(movie.id, movie.isFavorite)
        }
    }

    // Series Actions
    fun selectSeriesGenre(genre: String) {
        _selectedSeriesGenre.value = genre
    }

    fun setSeriesSearchQuery(query: String) {
        _seriesSearchQuery.value = query
    }

    fun getEpisodesForSeries(seriesId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.model.EpisodeEntity>> {
        return repository.getEpisodesForSeries(seriesId)
    }

    fun playEpisode(series: com.example.data.model.SeriesEntity, episode: com.example.data.model.EpisodeEntity) {
        val playable = episode.toPlayableChannel(series)
        playChannel(playable)
    }

    fun toggleSeriesFavorite(series: com.example.data.model.SeriesEntity) {
        viewModelScope.launch {
            repository.toggleSeriesFavorite(series.id, series.isFavorite)
        }
    }

    fun toggleWatchlist(channel: ChannelEntity, watchlistName: String) {
        viewModelScope.launch {
            repository.toggleWatchlistMembership(channel.id, watchlistName)
        }
    }

    fun createWatchlist(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.createWatchlist(name, colorHex)
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.switchActiveProfile(profileId)
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
        }
    }

    fun loadOpenSourcesPlaylist() {
        _addProfileState.value = AddProfileUiState(isLoading = true)
        viewModelScope.launch {
            try {
                repository.loadOpenSourcesPlaylist()
                _addProfileState.value = AddProfileUiState(
                    isLoading = false,
                    successMessage = "Open sources playlist loaded!"
                )
            } catch (e: Exception) {
                _addProfileState.value = AddProfileUiState(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load open sources"
                )
            }
        }
    }

    fun addXtreamProfile(name: String, server: String, user: String, pass: String) {
        _addProfileState.value = AddProfileUiState(isLoading = true)
        viewModelScope.launch {
            val result = repository.addXtreamProfile(name, server, user, pass)
            result.fold(
                onSuccess = {
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        successMessage = "Xtream Codes playlist connected!"
                    )
                },
                onFailure = { err ->
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Connection failed"
                    )
                }
            )
        }
    }

    fun addM3uProfile(name: String, urlOrContent: String, isRaw: Boolean, epgUrl: String) {
        _addProfileState.value = AddProfileUiState(isLoading = true)
        viewModelScope.launch {
            val result = repository.addM3uProfile(name, urlOrContent, isRaw, epgUrl)
            result.fold(
                onSuccess = {
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        successMessage = "M3U Playlist imported successfully!"
                    )
                },
                onFailure = { err ->
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to import M3U playlist"
                    )
                }
            )
        }
    }

    fun addStalkerProfile(name: String, portalUrl: String, macAddress: String) {
        _addProfileState.value = AddProfileUiState(isLoading = true)
        viewModelScope.launch {
            val result = repository.addStalkerProfile(name, portalUrl, macAddress)
            result.fold(
                onSuccess = {
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        successMessage = "Stalker Portal connected successfully!"
                    )
                },
                onFailure = { err ->
                    _addProfileState.value = AddProfileUiState(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to connect to Stalker Portal"
                    )
                }
            )
        }
    }

    fun resetAddProfileState() {
        _addProfileState.value = AddProfileUiState()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
