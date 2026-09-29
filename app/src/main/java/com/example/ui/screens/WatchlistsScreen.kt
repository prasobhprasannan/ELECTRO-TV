package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import coil.compose.AsyncImage
import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.WatchlistEntity
import com.example.player.PlayerUiState
import com.example.ui.components.YouTubeCompactVideoRow
import com.example.ui.components.YouTubeVideoFeedCard
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed

@Composable
fun WatchlistsScreen(
    favoriteChannels: List<ChannelEntity>,
    recentChannels: List<ChannelEntity>,
    allChannels: List<ChannelEntity>,
    watchlists: List<WatchlistEntity>,
    playerState: PlayerUiState,
    epgMap: Map<String, Pair<EpgProgramEntity?, EpgProgramEntity?>>,
    onChannelClick: (ChannelEntity) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit,
    onWatchlistClick: (ChannelEntity) -> Unit,
    onOpenCreateWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filterChips = remember(watchlists) {
        listOf("All Subscriptions", "Live Now", "Favorites") + watchlists.map { it.name }
    }

    val displayChannels = when (selectedFilterIndex) {
        0 -> if (favoriteChannels.isNotEmpty()) favoriteChannels else allChannels.take(10)
        1 -> allChannels
        2 -> favoriteChannels
        else -> {
            val wlName = filterChips.getOrNull(selectedFilterIndex) ?: ""
            allChannels.filter { it.watchlistNames.split(",").contains(wlName) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeDarkBg)
    ) {
        // YouTube Subscriptions Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subscriptions,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subscriptions",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = Color(0xFF272727),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onOpenCreateWatchlist() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = YouTubeBlue, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New List",
                        color = YouTubeBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // YouTube Channel Circles Carousel along top
        val subscribedChannels = favoriteChannels.ifEmpty { allChannels.take(8) }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subscribedChannels, key = { it.id }) { ch ->
                val isPlaying = playerState.currentChannel?.id == ch.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(62.dp)
                        .clickable { onChannelClick(ch) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF272727))
                            .border(
                                width = if (isPlaying) 2.dp else 1.5.dp,
                                color = if (isPlaying) YouTubeRed else Color(0xFF404040),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!ch.logoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ch.logoUrl,
                                contentDescription = ch.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = YouTubeBlue, modifier = Modifier.size(24.dp))
                        }

                        // Live broadcast dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(YouTubeRed)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ch.name,
                        color = if (isPlaying) Color.White else TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterChips.forEachIndexed { index, name ->
                val isSelected = selectedFilterIndex == index
                Surface(
                    color = if (isSelected) YouTubePillActiveBg else YouTubePillBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { selectedFilterIndex = index }
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) YouTubePillActiveText else Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Subscribed Feed Items (16:9 Video Feed)
        if (displayChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Channels in this Watchlist",
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Star channels from Live TV or tap the bookmark icon to organize them.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayChannels, key = { it.id }) { channel ->
                    val isPlaying = playerState.currentChannel?.id == channel.id
                    val epgPair = epgMap[channel.tvgId ?: channel.id]
                    YouTubeVideoFeedCard(
                        channel = channel,
                        currentProgram = epgPair?.first,
                        nextProgram = epgPair?.second,
                        isPlaying = isPlaying,
                        onChannelClick = { onChannelClick(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) },
                        onWatchlistClick = { onWatchlistClick(channel) }
                    )
                }
            }
        }
    }
}
