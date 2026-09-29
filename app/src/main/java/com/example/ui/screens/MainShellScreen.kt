package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.ChannelEntity
import com.example.ui.components.AddPlaylistBottomSheet
import com.example.ui.components.WatchlistManageBottomSheet
import com.example.ui.components.getThumbnailUrl
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun MainShellScreen(
    viewModel: IptvViewModel = viewModel()
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showAddPlaylistSheet by remember { mutableStateOf(false) }
    var showAboutScreen by remember { mutableStateOf(false) }
    var channelForWatchlistSheet by remember { mutableStateOf<ChannelEntity?>(null) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val playerState by viewModel.playerUiState.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState()
    val filteredChannels by viewModel.filteredChannels.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val recentChannels by viewModel.recentChannels.collectAsState()
    val watchlists by viewModel.watchlists.collectAsState()
    val epgMap by viewModel.epgMap.collectAsState()
    val addProfileState by viewModel.addProfileState.collectAsState()

    // Movies state
    val filteredMovies by viewModel.filteredMovies.collectAsState()
    val selectedMovieGenre by viewModel.selectedMovieGenre.collectAsState()
    val movieSearchQuery by viewModel.movieSearchQuery.collectAsState()

    // Series state
    val filteredSeries by viewModel.filteredSeries.collectAsState()
    val selectedSeriesGenre by viewModel.selectedSeriesGenre.collectAsState()
    val seriesSearchQuery by viewModel.seriesSearchQuery.collectAsState()

    val currentChannel = playerState.currentChannel
    val currentEpgPair = if (currentChannel != null) epgMap[currentChannel.tvgId ?: currentChannel.id] else null

    // When user plays a channel, open the full YouTube player view
    val onPlayChannel: (ChannelEntity) -> Unit = { ch ->
        viewModel.playChannel(ch)
        isPlayerExpanded = true
    }

    // Handle back button when player is expanded
    BackHandler(enabled = isPlayerExpanded && !isFullscreen) {
        isPlayerExpanded = false
    }

    // Fullscreen Immersive Mode or Expanded YouTube Player View
    if (currentChannel != null && (isFullscreen || isPlayerExpanded)) {
        YouTubePlayerScreen(
            playerManager = viewModel.playerManager,
            playerState = playerState,
            currentProgram = currentEpgPair?.first,
            nextProgram = currentEpgPair?.second,
            recommendedChannels = allChannels,
            onChannelSelect = { ch -> viewModel.playChannel(ch) },
            onZapNext = { viewModel.zapNextChannel() },
            onZapPrev = { viewModel.zapPreviousChannel() },
            onToggleFavorite = { ch -> viewModel.toggleFavorite(ch) },
            onWatchlistClick = { ch -> channelForWatchlistSheet = ch },
            isFullscreen = isFullscreen,
            onToggleFullscreen = { viewModel.setFullscreen(!isFullscreen) },
            onMinimizePlayer = { isPlayerExpanded = false },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        // Watchlist Assignment Sheet if opened from Player
        if (channelForWatchlistSheet != null) {
            WatchlistManageBottomSheet(
                channel = channelForWatchlistSheet!!,
                watchlists = watchlists,
                onToggleWatchlist = { wlName ->
                    viewModel.toggleWatchlist(channelForWatchlistSheet!!, wlName)
                },
                onCreateWatchlist = { name, colorHex ->
                    viewModel.createWatchlist(name, colorHex)
                },
                onDismiss = { channelForWatchlistSheet = null }
            )
        }
        return
    }

    if (showAboutScreen) {
        AboutScreen(
            onBack = { showAboutScreen = false },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
        return
    }

    Scaffold(
        containerColor = YouTubeDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // YouTube Mini-Player Bar (docked directly above the bottom navigation bar)
                AnimatedVisibility(
                    visible = currentChannel != null && !isPlayerExpanded,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    if (currentChannel != null) {
                        Surface(
                            color = Color(0xFF212121),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable { isPlayerExpanded = true }
                                .testTag("electro_mini_player_bar")
                        ) {
                            Column {
                                // Electro Cyan live timeline progress line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .background(Color(0xFF00E5FF))
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // 16:9 Mini Live Preview Window
                                        Box(
                                            modifier = Modifier
                                                .width(76.dp)
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black)
                                        ) {
                                            AsyncImage(
                                                model = currentChannel.getThumbnailUrl(),
                                                contentDescription = currentChannel.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Red LIVE badge
                                            Surface(
                                                color = YouTubeRed,
                                                shape = RoundedCornerShape(2.dp),
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(2.dp)
                                            ) {
                                                Text(
                                                    text = "LIVE",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            val title = currentEpgPair?.first?.title ?: currentChannel.name
                                            Text(
                                                text = title,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${currentChannel.name} • Playing now",
                                                color = TextSecondaryDark,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Play / Pause and Close
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.playerManager.togglePlayPause() },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.playerManager.release()
                                                isPlayerExpanded = false
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // YouTube Bottom Navigation Bar with Live TV, Movies, Series, EPG, You
                NavigationBar(
                    containerColor = Color(0xFF0F0F0F),
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 0.5.dp,
                        color = Color(0xFF272727)
                    )
                ) {
                    val navItems = listOf(
                        Triple("Live TV", Icons.Filled.LiveTv to Icons.Outlined.LiveTv, 0),
                        Triple("Movies", Icons.Filled.Movie to Icons.Outlined.Movie, 1),
                        Triple("Series", Icons.Filled.Tv to Icons.Outlined.Tv, 2),
                        Triple("EPG Guide", Icons.Filled.CalendarMonth to Icons.Outlined.CalendarMonth, 3),
                        Triple("You", Icons.Filled.AccountCircle to Icons.Outlined.AccountCircle, 4)
                    )

                    navItems.forEach { (label, iconPair, index) ->
                        val selected = selectedNavIndex == index
                        val icon = if (selected) iconPair.first else iconPair.second

                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedNavIndex = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color(0xFFAAAAAA),
                                unselectedTextColor = Color(0xFFAAAAAA)
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            when (selectedNavIndex) {
                0 -> LiveChannelsScreen(
                    channels = filteredChannels,
                    allChannels = allChannels,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    isGridView = isGridView,
                    onToggleViewMode = { viewModel.toggleViewMode() },
                    playerState = playerState,
                    epgMap = epgMap,
                    onChannelClick = onPlayChannel,
                    onToggleFavorite = { ch -> viewModel.toggleFavorite(ch) },
                    onWatchlistClick = { ch -> channelForWatchlistSheet = ch },
                    onOpenAddPlaylist = {
                        viewModel.resetAddProfileState()
                        showAddPlaylistSheet = true
                    },
                    onOpenAbout = { showAboutScreen = true }
                )

                1 -> MoviesScreen(
                    movies = filteredMovies,
                    selectedGenre = selectedMovieGenre,
                    onSelectGenre = { viewModel.selectMovieGenre(it) },
                    searchQuery = movieSearchQuery,
                    onSearchQueryChange = { viewModel.setMovieSearchQuery(it) },
                    onPlayMovie = { movie ->
                        viewModel.playMovie(movie)
                        isPlayerExpanded = true
                    },
                    onToggleFavorite = { movie -> viewModel.toggleMovieFavorite(movie) },
                    onOpenAddPlaylist = {
                        viewModel.resetAddProfileState()
                        showAddPlaylistSheet = true
                    }
                )

                2 -> SeriesScreen(
                    seriesList = filteredSeries,
                    selectedGenre = selectedSeriesGenre,
                    onSelectGenre = { viewModel.selectSeriesGenre(it) },
                    searchQuery = seriesSearchQuery,
                    onSearchQueryChange = { viewModel.setSeriesSearchQuery(it) },
                    onToggleFavorite = { series -> viewModel.toggleSeriesFavorite(series) },
                    getEpisodesForSeries = { seriesId -> viewModel.getEpisodesForSeries(seriesId) },
                    onPlayEpisode = { series, episode ->
                        viewModel.playEpisode(series, episode)
                        isPlayerExpanded = true
                    },
                    onOpenAddPlaylist = {
                        viewModel.resetAddProfileState()
                        showAddPlaylistSheet = true
                    }
                )

                3 -> EpgGuideScreen(
                    channels = allChannels,
                    epgMap = epgMap,
                    playerState = playerState,
                    onWatchChannel = onPlayChannel
                )

                4 -> PlaylistsScreen(
                    profiles = profiles,
                    activeProfile = activeProfile,
                    recentChannels = recentChannels,
                    watchlists = watchlists,
                    onSwitchProfile = { viewModel.switchProfile(it) },
                    onDeleteProfile = { viewModel.deleteProfile(it) },
                    onOpenAddPlaylist = {
                        viewModel.resetAddProfileState()
                        showAddPlaylistSheet = true
                    },
                    onOpenAbout = { showAboutScreen = true },
                    onChannelClick = onPlayChannel
                )
            }
        }
    }

    // Add Playlist Sheet
    if (showAddPlaylistSheet) {
        AddPlaylistBottomSheet(
            onDismiss = { showAddPlaylistSheet = false },
            addProfileState = addProfileState,
            onAddXtream = { name, server, user, pass ->
                viewModel.addXtreamProfile(name, server, user, pass)
            },
            onAddM3u = { name, urlOrContent, isRaw, epgUrl ->
                viewModel.addM3uProfile(name, urlOrContent, isRaw, epgUrl)
            },
            onAddStalker = { name, portalUrl, macAddress ->
                viewModel.addStalkerProfile(name, portalUrl, macAddress)
            }
        )
    }

    // Watchlist Assignment Sheet
    val targetChannel = channelForWatchlistSheet
    if (targetChannel != null) {
        WatchlistManageBottomSheet(
            channel = targetChannel,
            watchlists = watchlists,
            onToggleWatchlist = { wlName ->
                viewModel.toggleWatchlist(targetChannel, wlName)
            },
            onCreateWatchlist = { name, colorHex ->
                viewModel.createWatchlist(name, colorHex)
            },
            onDismiss = { channelForWatchlistSheet = null }
        )
    }
}
