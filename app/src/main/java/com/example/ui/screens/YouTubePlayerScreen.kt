package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.player.IptvPlayerManager
import com.example.player.PlaybackStatus
import com.example.player.PlayerUiState
import com.example.ui.components.YouTubeCompactVideoRow
import com.example.ui.components.StreamSettingsBottomSheet
import com.example.ui.components.getQualityBadge
import com.example.ui.components.getThumbnailUrl
import com.example.ui.components.getViewersCountFormatted
import com.example.ui.theme.AmberGold
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubeDarkCard
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun YouTubePlayerScreen(
    playerManager: IptvPlayerManager,
    playerState: PlayerUiState,
    currentProgram: EpgProgramEntity?,
    nextProgram: EpgProgramEntity?,
    recommendedChannels: List<ChannelEntity>,
    onChannelSelect: (ChannelEntity) -> Unit,
    onZapNext: () -> Unit,
    onZapPrev: () -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit,
    onWatchlistClick: (ChannelEntity) -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onMinimizePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentChannel = playerState.currentChannel ?: return
    var showControls by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: EPG Guide & Description, 1: Live Chat / Up Next
    var showStreamSettings by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Auto-hide player controls overlay after 4 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    // Handle landscape orientation in fullscreen
    DisposableEffect(isFullscreen) {
        val activity = context as? Activity
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        onDispose {
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        if (isFullscreen) {
            onToggleFullscreen()
        } else {
            onMinimizePlayer()
        }
    }

    if (isFullscreen) {
        // Fullscreen Landscape Video Player
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
                .testTag("electro_fullscreen_player")
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerManager.getPlayer()
                        useController = false
                        resizeMode = playerState.aspectRatioMode.resizeMode
                    }
                },
                update = { view ->
                    view.player = playerManager.getPlayer()
                    view.resizeMode = playerState.aspectRatioMode.resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Indicator
            if (playerState.status == PlaybackStatus.BUFFERING) {
                CircularProgressIndicator(
                    color = YouTubeRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onToggleFullscreen) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentProgram?.title ?: currentChannel.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentChannel.name} • LIVE • ${currentChannel.getQualityBadge()}",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showStreamSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Stream Settings", tint = Color.White)
                            }
                            IconButton(onClick = { playerManager.cycleAspectRatio() }) {
                                Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                            }
                            IconButton(onClick = { playerManager.toggleMute() }) {
                                Icon(
                                    imageVector = if (playerState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = if (playerState.isMuted) YouTubeRed else Color.White
                                )
                            }
                            IconButton(onClick = onToggleFullscreen) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                            }
                        }
                    }

                    // Center Zap & Play/Pause controls
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        IconButton(
                            onClick = onZapPrev,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        IconButton(
                            onClick = { playerManager.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(YouTubeRed)
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        IconButton(
                            onClick = onZapNext,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            if (showStreamSettings) {
                StreamSettingsBottomSheet(
                    playerState = playerState,
                    onSelectVideoTrack = { playerManager.selectVideoTrack(it) },
                    onSelectAudioTrack = { playerManager.selectAudioTrack(it) },
                    onSelectSubtitleTrack = { playerManager.selectSubtitleTrack(it) },
                    onDismiss = { showStreamSettings = false }
                )
            }
        }
        return
    }

    // Standard YouTube Portrait Player View (16:9 player on top, scrollable YouTube details & feed below)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeDarkBg)
    ) {
        // 16:9 Video Player Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
                .testTag("electro_video_viewport")
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerManager.getPlayer()
                        useController = false
                        resizeMode = playerState.aspectRatioMode.resizeMode
                    }
                },
                update = { view ->
                    view.player = playerManager.getPlayer()
                    view.resizeMode = playerState.aspectRatioMode.resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Spinner
            if (playerState.status == PlaybackStatus.BUFFERING) {
                CircularProgressIndicator(
                    color = YouTubeRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                )
            }

            // Error Overlay
            if (playerState.status == PlaybackStatus.ERROR) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Stream Unavailable", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(playerState.errorMessage ?: "Failed to load", color = TextSecondaryDark, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = YouTubeRed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { playerManager.retryCurrentStream() }
                        ) {
                            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                        }
                    }
                }
            }

            // YouTube Video HUD Controls
            androidx.compose.animation.AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                ) {
                    // Top: Down Chevron (minimize to YouTube mini player) & Aspect/Mute/Fullscreen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onMinimizePlayer,
                            modifier = Modifier.testTag("btn_minimize_player")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showStreamSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Stream Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { playerManager.cycleAspectRatio() }) {
                                Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { playerManager.toggleMute() }) {
                                Icon(
                                    imageVector = if (playerState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = if (playerState.isMuted) YouTubeRed else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onToggleFullscreen) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    // Center Play/Pause button
                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("btn_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Bottom: Live Broadcast Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(YouTubeRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${currentChannel.getViewersCountFormatted()} watching now",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Scrollable Details, Channel Row, Action Pills, and Recommendations
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Video Program Title
                    val mainTitle = currentProgram?.title ?: "${currentChannel.name} 24/7 Live Stream"
                    Text(
                        text = mainTitle,
                        color = TextPrimaryDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Viewer stats line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentChannel.getViewersCountFormatted()} views • Started streaming live",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentChannel.getQualityBadge(),
                                color = YouTubeBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Channel Profile & Subscribe Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF272727))
                                    .border(1.dp, Color(0xFF383838), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!currentChannel.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentChannel.logoUrl,
                                        contentDescription = currentChannel.name,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = YouTubeBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentChannel.name,
                                        color = TextPrimaryDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFFAAAAAA),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "${currentChannel.groupTitle} • Channel #${currentChannel.channelNumber}",
                                    color = TextSecondaryDark,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        // YouTube-style Subscribe / Favorite Pill Button
                        Surface(
                            color = if (currentChannel.isFavorite) Color(0xFF272727) else Color.White,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clickable { onToggleFavorite(currentChannel) }
                                .testTag("btn_player_favorite")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                if (currentChannel.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Favorited",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Favorite",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Pills Bar (Horizontal Scrollable YouTube Pills)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbs up / Favorite
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onToggleFavorite(currentChannel) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentChannel.isFavorite) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (currentChannel.isFavorite) YouTubeBlue else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentChannel.isFavorite) "Liked" else "Like",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Save to Watchlist
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onWatchlistClick(currentChannel) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentChannel.watchlistNames.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (currentChannel.watchlistNames.isNotEmpty()) YouTubeBlue else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentChannel.watchlistNames.isNotEmpty()) "Saved" else "Save",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Next Channel Zap
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onZapNext() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Zap Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zap Next",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Aspect Ratio Mode
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { playerManager.cycleAspectRatio() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = playerState.aspectRatioMode.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Stream Settings (Resolution, Audio, Subtitles)
                        Surface(
                            color = Color(0xFF272727),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clickable { showStreamSettings = true }
                                .testTag("btn_player_stream_settings")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Stream Settings",
                                    tint = YouTubeBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stream Settings",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // EPG Live Program Guide Card (YouTube Description / Live Status block)
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = YouTubeRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE EPG PROGRAM",
                                        color = YouTubeRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                if (currentProgram != null) {
                                    Text(
                                        text = "${timeFormat.format(Date(currentProgram.startTimeEpoch))} - ${timeFormat.format(Date(currentProgram.endTimeEpoch))}",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = currentProgram?.title ?: currentChannel.name,
                                color = TextPrimaryDark,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (currentProgram?.description?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentProgram.description,
                                    color = TextSecondaryDark,
                                    fontSize = 11.5.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (currentProgram != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val now = System.currentTimeMillis()
                                val dur = (currentProgram.endTimeEpoch - currentProgram.startTimeEpoch).coerceAtLeast(1L)
                                val el = (now - currentProgram.startTimeEpoch).coerceIn(0L, dur)
                                val prog = (el.toFloat() / dur.toFloat()).coerceIn(0f, 1f)

                                LinearProgressIndicator(
                                    progress = { prog },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = YouTubeRed,
                                    trackColor = Color(0xFF333333)
                                )
                            }

                            if (nextProgram != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Up Next (${timeFormat.format(Date(nextProgram.startTimeEpoch))}): ${nextProgram.title}",
                                    color = TextMutedDark,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // YouTube Recommendations Header
                    Text(
                        text = "Related Live Channels",
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // List of recommended live channels (compact YouTube cards)
            items(recommendedChannels.filter { it.id != currentChannel.id }) { ch ->
                YouTubeCompactVideoRow(
                    channel = ch,
                    currentProgram = null,
                    isPlaying = false,
                    onChannelClick = { onChannelSelect(ch) },
                    onToggleFavorite = { onToggleFavorite(ch) },
                    onWatchlistClick = { onWatchlistClick(ch) }
                )
            }
        }
    }

    if (showStreamSettings) {
        StreamSettingsBottomSheet(
            playerState = playerState,
            onSelectVideoTrack = { playerManager.selectVideoTrack(it) },
            onSelectAudioTrack = { playerManager.selectAudioTrack(it) },
            onSelectSubtitleTrack = { playerManager.selectSubtitleTrack(it) },
            onDismiss = { showStreamSettings = false }
        )
    }
}
