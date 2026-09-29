package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.LiveBroadcastRed
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightDarkCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubeDarkCard
import com.example.ui.theme.YouTubeRed

/**
 * YouTube-style full-width video card with 16:9 thumbnail, live badge,
 * viewer count, channel avatar, verified tag, and EPG highlight.
 */
@Composable
fun YouTubeVideoFeedCard(
    channel: ChannelEntity,
    currentProgram: EpgProgramEntity?,
    nextProgram: EpgProgramEntity? = null,
    isPlaying: Boolean,
    onChannelClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onChannelClick)
            .testTag("youtube_video_card_${channel.id}")
            .padding(bottom = 16.dp)
    ) {
        // 16:9 Video Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF181818))
        ) {
            // Thumbnail Image
            AsyncImage(
                model = channel.getThumbnailUrl(),
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Subtle dark gradient at top and bottom for high badge legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Top Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel group / category pill
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = channel.groupTitle.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Stream Quality Badge (4K / 1080p / HD)
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = channel.getQualityBadge(),
                        color = Color(0xFF3EA6FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom Badges Row (YouTube signature LIVE badge & viewer count)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // YouTube LIVE pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(YouTubeRed)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Viewer count badge
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = "${channel.getViewersCountFormatted()} watching",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // If Currently Playing, show YouTube Red playback bar at bottom edge
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(YouTubeRed)
                )
            }
        }

        // Details Row below thumbnail: Channel Avatar, Title, Channel Subtitle, More Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar Circle (38dp)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF272727))
                    .border(1.dp, Color(0xFF383838), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF3EA6FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(modifier = Modifier.weight(1f)) {
                // Main Title (Program title if on air, otherwise channel name)
                val displayTitle = currentProgram?.title ?: "${channel.name} - 24/7 Live Stream"
                Text(
                    text = displayTitle,
                    color = TextPrimaryDark,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // YouTube-style subtitle: Channel Name • Viewers • Live now
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Live now",
                        color = TextMutedDark,
                        fontSize = 12.sp
                    )
                }

                // EPG Up Next line if present
                if (nextProgram != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Up next: ${nextProgram.title}",
                        color = TextMutedDark,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // YouTube 3-dots more menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF282828))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (channel.watchlistNames.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (channel.watchlistNames.isNotEmpty()) Color(0xFF3EA6FF) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (channel.watchlistNames.isNotEmpty()) "Edit Watchlists" else "Save to Watchlist",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onWatchlistClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (channel.isFavorite) AmberGold else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (channel.isFavorite) "Remove Favorite" else "Add to Favorites",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onToggleFavorite()
                        }
                    )
                }
            }
        }
    }
}

/**
 * YouTube Compact Video Row (Used for search results, recommendations, and list mode)
 */
@Composable
fun YouTubeCompactVideoRow(
    channel: ChannelEntity,
    currentProgram: EpgProgramEntity?,
    isPlaying: Boolean,
    onChannelClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onChannelClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 16:9 Thumbnail Box
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            AsyncImage(
                model = channel.getThumbnailUrl(),
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // LIVE Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(YouTubeRed)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(YouTubeRed)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & metadata
        Column(modifier = Modifier.weight(1f)) {
            val title = currentProgram?.title ?: channel.name
            Text(
                text = title,
                color = TextPrimaryDark,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${channel.name} • ${channel.getViewersCountFormatted()} watching",
                color = TextSecondaryDark,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = channel.groupTitle,
                color = TextMutedDark,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Actions
        IconButton(
            onClick = onWatchlistClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (channel.watchlistNames.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Watchlist",
                tint = if (channel.watchlistNames.isNotEmpty()) Color(0xFF3EA6FF) else TextSecondaryDark,
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorite",
                tint = if (channel.isFavorite) AmberGold else TextSecondaryDark,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Backward compatibility alias for any existing references
@Composable
fun ChannelGridCard(
    channel: ChannelEntity,
    currentProgram: EpgProgramEntity?,
    isPlaying: Boolean,
    onChannelClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    YouTubeVideoFeedCard(
        channel = channel,
        currentProgram = currentProgram,
        isPlaying = isPlaying,
        onChannelClick = onChannelClick,
        onToggleFavorite = onToggleFavorite,
        onWatchlistClick = onWatchlistClick,
        modifier = modifier
    )
}

@Composable
fun ChannelListRow(
    channel: ChannelEntity,
    currentProgram: EpgProgramEntity?,
    isPlaying: Boolean,
    onChannelClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    YouTubeCompactVideoRow(
        channel = channel,
        currentProgram = currentProgram,
        isPlaying = isPlaying,
        onChannelClick = onChannelClick,
        onToggleFavorite = onToggleFavorite,
        onWatchlistClick = onWatchlistClick,
        modifier = modifier
    )
}
