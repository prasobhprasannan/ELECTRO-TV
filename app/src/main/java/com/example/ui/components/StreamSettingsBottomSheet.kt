package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.AudioTrackOption
import com.example.player.PlayerUiState
import com.example.player.SubtitleTrackOption
import com.example.player.VideoTrackOption
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamSettingsBottomSheet(
    playerState: PlayerUiState,
    onSelectVideoTrack: (VideoTrackOption) -> Unit,
    onSelectAudioTrack: (AudioTrackOption) -> Unit,
    onSelectSubtitleTrack: (SubtitleTrackOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF555555))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("stream_settings_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = YouTubeBlue.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = YouTubeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Stream Settings",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playerState.currentChannel?.name ?: "Current Broadcast",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Settings Tabs: Resolution, Audio, Subtitles
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF181818),
                contentColor = YouTubeBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = YouTubeBlue,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resolution", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color(0xFFAAAAAA)
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Audio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color(0xFFAAAAAA)
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ClosedCaption, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subtitles", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color(0xFFAAAAAA)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Video Resolution Tab
                    VideoResolutionSection(
                        playerState = playerState,
                        onSelectTrack = {
                            onSelectVideoTrack(it)
                            onDismiss()
                        }
                    )
                }
                1 -> {
                    // Audio Tracks Tab
                    AudioTracksSection(
                        playerState = playerState,
                        onSelectTrack = {
                            onSelectAudioTrack(it)
                            onDismiss()
                        }
                    )
                }
                2 -> {
                    // Subtitles Tab
                    SubtitlesSection(
                        playerState = playerState,
                        onSelectTrack = {
                            onSelectSubtitleTrack(it)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoResolutionSection(
    playerState: PlayerUiState,
    onSelectTrack: (VideoTrackOption) -> Unit
) {
    val tracks = playerState.videoTracks
    val currentResolution = playerState.videoResolution.ifEmpty { "1080p FHD (Auto)" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Active Stream Resolution status badge
        Surface(
            color = Color(0xFF272727),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = YouTubeBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Current Stream Resolution",
                        color = TextSecondaryDark,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = currentResolution,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (tracks.isEmpty() || (tracks.size == 1 && tracks[0].isAuto)) {
            // Single stream broadcast info
            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "This live stream is served at a fixed broadcast resolution ($currentResolution). Multi-bitrate switching is not provided by this feed.",
                        color = TextMutedDark,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            Text(
                text = "Select Video Quality",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tracks) { option ->
                    val isSelected = option.isSelected

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2C2C2C) else Color(0xFF222222))
                            .border(1.dp, if (isSelected) YouTubeBlue else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onSelectTrack(option) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.label,
                                color = if (isSelected) Color.White else TextPrimaryDark,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = YouTubeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioTracksSection(
    playerState: PlayerUiState,
    onSelectTrack: (AudioTrackOption) -> Unit
) {
    val audioTracks = playerState.audioTracks

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (audioTracks.isEmpty()) {
            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Default Audio Stream Active",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Stereo AAC • Single audio track embedded in broadcast feed.",
                            color = TextMutedDark,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Available Audio Tracks (${audioTracks.size})",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(audioTracks) { track ->
                    val isSelected = track.isSelected

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2C2C2C) else Color(0xFF222222))
                            .border(1.dp, if (isSelected) YouTubeBlue else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onSelectTrack(track) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = if (isSelected) YouTubeBlue else Color(0xFFAAAAAA),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = track.label,
                                color = if (isSelected) Color.White else TextPrimaryDark,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = YouTubeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitlesSection(
    playerState: PlayerUiState,
    onSelectTrack: (SubtitleTrackOption) -> Unit
) {
    val subtitleTracks = playerState.subtitleTracks
    val hasContentSubtitles = subtitleTracks.any { !it.isOff }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (!hasContentSubtitles) {
            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "No Subtitles in Stream",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "This channel does not broadcast closed captions or subtitle tracks.",
                            color = TextMutedDark,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Subtitles & Closed Captions",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(subtitleTracks) { track ->
                    val isSelected = track.isSelected

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2C2C2C) else Color(0xFF222222))
                            .border(1.dp, if (isSelected) YouTubeBlue else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onSelectTrack(track) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = null,
                                tint = if (isSelected) YouTubeBlue else Color(0xFFAAAAAA),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = track.label,
                                color = if (isSelected) Color.White else TextPrimaryDark,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = YouTubeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
