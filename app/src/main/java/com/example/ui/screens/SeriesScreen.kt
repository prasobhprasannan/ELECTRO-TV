package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubeDarkCard
import com.example.ui.theme.YouTubeDarkSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.flow.Flow

@Composable
fun SeriesScreen(
    seriesList: List<SeriesEntity>,
    selectedGenre: String,
    onSelectGenre: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (SeriesEntity) -> Unit,
    getEpisodesForSeries: (String) -> Flow<List<EpisodeEntity>>,
    onPlayEpisode: (SeriesEntity, EpisodeEntity) -> Unit,
    onOpenAddPlaylist: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedSeriesForEpisodes by remember { mutableStateOf<SeriesEntity?>(null) }

    val genres = remember(seriesList) {
        val distinct = seriesList.map { it.genre }.distinct().filter { it.isNotBlank() }
        listOf("All", "Favorites") + distinct
    }

    val featuredSeries = remember(seriesList) {
        seriesList.firstOrNull { it.isFavorite } ?: seriesList.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeDarkBg)
    ) {
        // YouTube-style Header for Series
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Electro series icon badge
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF4F46E5)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Electro",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Series",
                        color = YouTubeBlue,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF272727),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "SHOWS",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Top action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Cast to TV */ }) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "Cast",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onSearchQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Series",
                        tint = if (isSearchExpanded || searchQuery.isNotBlank()) YouTubeBlue else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Big, Prominent Search Bar (Larger, perfectly aligned, no clipped text)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search series, seasons, cast...",
                        color = TextMutedDark,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotEmpty()) YouTubeBlue else TextSecondaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = YouTubeDarkSurface,
                    unfocusedContainerColor = YouTubeDarkSurface,
                    focusedBorderColor = YouTubeBlue,
                    unfocusedBorderColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(28.dp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("series_search_input")
            )
        }

        // YouTube Genre Pills Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            genres.forEach { genre ->
                val isSelected = genre.equals(selectedGenre, ignoreCase = true)
                Surface(
                    color = if (isSelected) YouTubePillActiveBg else YouTubePillBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .clickable { onSelectGenre(genre) }
                        .testTag("series_genre_pill_$genre")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (genre == "Favorites") {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else AmberGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = genre,
                            color = if (isSelected) YouTubePillActiveText else Color.White,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Series Catalog Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Featured Series Banner
            if (featuredSeries != null && selectedGenre == "All" && searchQuery.isBlank()) {
                item(span = { GridItemSpan(2) }) {
                    FeaturedSeriesHeroBanner(
                        series = featuredSeries,
                        onOpenEpisodes = { selectedSeriesForEpisodes = featuredSeries },
                        onToggleFavorite = { onToggleFavorite(featuredSeries) }
                    )
                }
            }

            // Section Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results (${seriesList.size})" else "$selectedGenre Series (${seriesList.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TV Shows & Multi-Season",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }

            // Series Poster Cards
            items(seriesList, key = { it.id }) { series ->
                SeriesPosterCard(
                    series = series,
                    onOpenEpisodes = { selectedSeriesForEpisodes = series },
                    onToggleFavorite = { onToggleFavorite(series) }
                )
            }

            if (seriesList.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No TV Series in Playlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Connect your IPTV service (Xtream, M3U, or Stalker) to explore series with full season & episode guides.", color = TextSecondaryDark, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (onOpenAddPlaylist != null) {
                                Button(
                                    onClick = onOpenAddPlaylist,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeBlue),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add IPTV Playlist", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Series Episodes Bottom Sheet Viewer
    val currentSeries = selectedSeriesForEpisodes
    if (currentSeries != null) {
        SeriesEpisodesBottomSheet(
            series = currentSeries,
            episodesFlow = getEpisodesForSeries(currentSeries.id),
            onDismiss = { selectedSeriesForEpisodes = null },
            onPlayEpisode = { ep ->
                onPlayEpisode(currentSeries, ep)
                selectedSeriesForEpisodes = null
            }
        )
    }
}

@Composable
fun FeaturedSeriesHeroBanner(
    series: SeriesEntity,
    onOpenEpisodes: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = YouTubeDarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenEpisodes() }
            .testTag("featured_series_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9.5f)
        ) {
            AsyncImage(
                model = series.backdropUrl ?: series.posterUrl,
                contentDescription = series.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = YouTubeRed,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "FEATURED SERIES",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (series.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (series.isFavorite) Color(0xFFF59E0B) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF333333),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = series.genre.uppercase(),
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⭐ ${series.rating}",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${series.seasonsCount} Season • ${series.releaseYear}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = series.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (series.plot.isNotBlank()) {
                    Text(
                        text = series.plot,
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onOpenEpisodes,
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Episodes", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesPosterCard(
    series: SeriesEntity,
    onOpenEpisodes: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = YouTubeDarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenEpisodes() }
            .testTag("series_card_${series.id}")
    ) {
        Column {
            // 2:3 Poster Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = series.posterUrl ?: series.backdropUrl,
                    contentDescription = series.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Rating Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⭐ ${series.rating}",
                            color = Color(0xFFFBBF24),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (series.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (series.isFavorite) Color(0xFFF59E0B) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Bottom Seasons Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${series.seasonsCount} Season",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Series Details
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = series.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = series.genre,
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = series.releaseYear,
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesEpisodesBottomSheet(
    series: SeriesEntity,
    episodesFlow: Flow<List<EpisodeEntity>>,
    onDismiss: () -> Unit,
    onPlayEpisode: (EpisodeEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val episodes by episodesFlow.collectAsState(initial = emptyList())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = YouTubeDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header with backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 8f)
            ) {
                AsyncImage(
                    model = series.backdropUrl ?: series.posterUrl,
                    contentDescription = series.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.85f),
                                    YouTubeDarkSurface
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = series.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${series.genre} • ⭐ ${series.rating} • ${episodes.size} Episodes Available",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Series Plot
            if (series.plot.isNotBlank()) {
                Text(
                    text = series.plot,
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Season tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = YouTubeRed,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Season 1",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${episodes.size} Episodes",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            // Episodes List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(episodes, key = { it.id }) { ep ->
                    EpisodeRowCard(
                        episode = ep,
                        onPlay = { onPlayEpisode(ep) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeRowCard(
    episode: EpisodeEntity,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = YouTubeDarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("episode_card_${episode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = episode.thumbnail,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Episode Number Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                ) {
                    Text(
                        text = "E${episode.episodeNumber}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.title}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = episode.duration,
                    color = YouTubeBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (episode.plot.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = episode.plot,
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
