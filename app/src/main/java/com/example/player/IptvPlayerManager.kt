package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.model.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class AspectRatioMode(val displayName: String, val resizeMode: Int) {
    FIT("Fit (16:9)", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Fill Screen", AspectRatioFrameLayout.RESIZE_MODE_FILL),
    ZOOM("Zoom / Crop", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    FIXED_4_3("4:3 Standard", AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
}

data class VideoTrackOption(
    val id: String,
    val label: String,
    val resolution: String,
    val bitrate: Int,
    val isSelected: Boolean,
    val isAuto: Boolean = false,
    val groupIndex: Int = -1,
    val trackIndex: Int = -1
)

data class AudioTrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val channels: Int,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)

data class SubtitleTrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val isSelected: Boolean,
    val isOff: Boolean = false,
    val groupIndex: Int = -1,
    val trackIndex: Int = -1
)

data class PlayerUiState(
    val currentChannel: ChannelEntity? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val errorMessage: String? = null,
    val isMuted: Boolean = false,
    val isPlaying: Boolean = false,
    val videoResolution: String = "",
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isPipActive: Boolean = false,
    val videoTracks: List<VideoTrackOption> = emptyList(),
    val audioTracks: List<AudioTrackOption> = emptyList(),
    val subtitleTracks: List<SubtitleTrackOption> = emptyList(),
    val selectedVideoTrackLabel: String = "Auto",
    val selectedAudioTrackLabel: String = "Default",
    val selectedSubtitleTrackLabel: String = "Off"
)

@OptIn(UnstableApi::class)
class IptvPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) VLC/3.0.18")
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(20000)
        .setAllowCrossProtocolRedirects(true)

    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            initPlayer()
        }
        return exoPlayer!!
    }

    private fun initPlayer() {
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _uiState.value = _uiState.value.copy(
                                    status = PlaybackStatus.BUFFERING,
                                    errorMessage = null
                                )
                            }
                            Player.STATE_READY -> {
                                _uiState.value = _uiState.value.copy(
                                    status = if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED,
                                    isPlaying = playWhenReady,
                                    errorMessage = null
                                )
                            }
                            Player.STATE_ENDED -> {
                                _uiState.value = _uiState.value.copy(
                                    status = PlaybackStatus.PAUSED,
                                    isPlaying = false
                                )
                            }
                            Player.STATE_IDLE -> {
                                _uiState.value = _uiState.value.copy(
                                    status = PlaybackStatus.IDLE
                                )
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = isPlaying,
                            status = if (isPlaying) PlaybackStatus.PLAYING else _uiState.value.status
                        )
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        updateTracksState(tracks)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val message = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Network connection failed"
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "Stream returned HTTP error (server unavailable)"
                            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "Malformed stream container"
                            PlaybackException.ERROR_CODE_TIMEOUT -> "Stream connection timed out"
                            else -> error.localizedMessage ?: "Failed to play stream"
                        }
                        _uiState.value = _uiState.value.copy(
                            status = PlaybackStatus.ERROR,
                            errorMessage = message,
                            isPlaying = false
                        )
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        if (videoSize.width > 0 && videoSize.height > 0) {
                            val qualityTag = when {
                                videoSize.width >= 3840 -> "4K UHD (${videoSize.width}x${videoSize.height})"
                                videoSize.width >= 1920 -> "FHD 1080p (${videoSize.width}x${videoSize.height})"
                                videoSize.width >= 1280 -> "HD 720p (${videoSize.width}x${videoSize.height})"
                                else -> "SD (${videoSize.width}x${videoSize.height})"
                            }
                            _uiState.value = _uiState.value.copy(videoResolution = qualityTag)
                        }
                    }
                })
            }
    }

    private fun updateTracksState(tracks: Tracks) {
        val vTracks = mutableListOf<VideoTrackOption>()
        val aTracks = mutableListOf<AudioTrackOption>()
        val sTracks = mutableListOf<SubtitleTrackOption>()

        var hasSelectedVideoOverride = false
        var selectedVideoLabel = "Auto"
        var selectedAudioLabel = "Default"
        var selectedSubtitleLabel = "Off"

        tracks.groups.forEachIndexed { groupIndex, group ->
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> {
                    for (trackIndex in 0 until group.length) {
                        val format = group.getTrackFormat(trackIndex)
                        val isSelected = group.isTrackSelected(trackIndex)
                        if (isSelected) hasSelectedVideoOverride = true
                        val width = format.width
                        val height = format.height
                        val bitrateKbps = if (format.bitrate > 0) format.bitrate / 1000 else 0
                        val resName = when {
                            height >= 2160 || width >= 3840 -> "4K UHD"
                            height >= 1080 || width >= 1920 -> "1080p FHD"
                            height >= 720 || width >= 1280 -> "720p HD"
                            height >= 480 -> "480p SD"
                            height > 0 -> "${height}p"
                            else -> "Stream Video"
                        }
                        val label = if (bitrateKbps > 0) {
                            "$resName (${width}x${height} • ${bitrateKbps} kbps)"
                        } else if (width > 0 && height > 0) {
                            "$resName (${width}x${height})"
                        } else {
                            "Video Track #${trackIndex + 1}"
                        }
                        if (isSelected) selectedVideoLabel = resName
                        vTracks.add(
                            VideoTrackOption(
                                id = "v_${groupIndex}_$trackIndex",
                                label = label,
                                resolution = if (width > 0) "${width}x$height" else "Auto",
                                bitrate = format.bitrate,
                                isSelected = isSelected,
                                isAuto = false,
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                }
                C.TRACK_TYPE_AUDIO -> {
                    for (trackIndex in 0 until group.length) {
                        val format = group.getTrackFormat(trackIndex)
                        val isSelected = group.isTrackSelected(trackIndex)
                        val lang = format.language?.uppercase() ?: ""
                        val channels = when (format.channelCount) {
                            1 -> "Mono"
                            2 -> "Stereo"
                            6 -> "5.1 Surround"
                            8 -> "7.1 Surround"
                            else -> if (format.channelCount > 0) "${format.channelCount}ch" else ""
                        }
                        val label = buildString {
                            if (!format.label.isNullOrBlank()) {
                                append(format.label)
                            } else if (lang.isNotBlank()) {
                                append("Audio ($lang)")
                            } else {
                                append("Audio Track #${trackIndex + 1}")
                            }
                            if (channels.isNotBlank()) append(" • $channels")
                            if (!format.codecs.isNullOrBlank()) append(" (${format.codecs})")
                        }
                        if (isSelected) selectedAudioLabel = label
                        aTracks.add(
                            AudioTrackOption(
                                id = "a_${groupIndex}_$trackIndex",
                                label = label,
                                language = format.language,
                                channels = format.channelCount,
                                isSelected = isSelected,
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                }
                C.TRACK_TYPE_TEXT -> {
                    for (trackIndex in 0 until group.length) {
                        val format = group.getTrackFormat(trackIndex)
                        val isSelected = group.isTrackSelected(trackIndex)
                        val lang = format.language?.uppercase() ?: ""
                        val label = buildString {
                            if (!format.label.isNullOrBlank()) {
                                append(format.label)
                            } else if (lang.isNotBlank()) {
                                append("Subtitles ($lang)")
                            } else {
                                append("Subtitles #${trackIndex + 1}")
                            }
                        }
                        if (isSelected) selectedSubtitleLabel = label
                        sTracks.add(
                            SubtitleTrackOption(
                                id = "s_${groupIndex}_$trackIndex",
                                label = label,
                                language = format.language,
                                isSelected = isSelected,
                                isOff = false,
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                }
            }
        }

        // Add Auto for Video
        val finalVideoTracks = mutableListOf<VideoTrackOption>()
        finalVideoTracks.add(
            VideoTrackOption(
                id = "video_auto",
                label = "Auto (Adaptive Quality)",
                resolution = "Auto",
                bitrate = 0,
                isSelected = !hasSelectedVideoOverride || selectedVideoLabel == "Auto",
                isAuto = true
            )
        )
        finalVideoTracks.addAll(vTracks)

        // Add Off option for Subtitles
        val finalSubtitleTracks = mutableListOf<SubtitleTrackOption>()
        finalSubtitleTracks.add(
            SubtitleTrackOption(
                id = "subtitle_off",
                label = "Off (Subtitles Disabled)",
                language = null,
                isSelected = sTracks.none { it.isSelected },
                isOff = true
            )
        )
        finalSubtitleTracks.addAll(sTracks)

        _uiState.value = _uiState.value.copy(
            videoTracks = finalVideoTracks,
            audioTracks = aTracks,
            subtitleTracks = finalSubtitleTracks,
            selectedVideoTrackLabel = if (finalVideoTracks.first().isSelected) "Auto" else selectedVideoLabel,
            selectedAudioTrackLabel = aTracks.firstOrNull { it.isSelected }?.label ?: if (aTracks.isNotEmpty()) aTracks.first().label else "Default",
            selectedSubtitleTrackLabel = if (finalSubtitleTracks.first().isSelected) "Off" else selectedSubtitleLabel
        )
    }

    fun selectVideoTrack(option: VideoTrackOption) {
        val player = exoPlayer ?: return
        try {
            if (option.isAuto) {
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                    .build()
            } else {
                val tracks = player.currentTracks
                val group = tracks.groups.getOrNull(option.groupIndex) ?: return
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setOverrideForType(
                        TrackSelectionOverride(group.mediaTrackGroup, option.trackIndex)
                    )
                    .build()
            }
            updateTracksState(player.currentTracks)
        } catch (_: Exception) {}
    }

    fun selectAudioTrack(option: AudioTrackOption) {
        val player = exoPlayer ?: return
        try {
            val tracks = player.currentTracks
            val group = tracks.groups.getOrNull(option.groupIndex) ?: return
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(
                    TrackSelectionOverride(group.mediaTrackGroup, option.trackIndex)
                )
                .build()
            updateTracksState(player.currentTracks)
        } catch (_: Exception) {}
    }

    fun selectSubtitleTrack(option: SubtitleTrackOption) {
        val player = exoPlayer ?: return
        try {
            if (option.isOff) {
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                    .build()
            } else {
                val tracks = player.currentTracks
                val group = tracks.groups.getOrNull(option.groupIndex) ?: return
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .setOverrideForType(
                        TrackSelectionOverride(group.mediaTrackGroup, option.trackIndex)
                    )
                    .build()
            }
            updateTracksState(player.currentTracks)
        } catch (_: Exception) {}
    }

    fun playChannel(channel: ChannelEntity) {
        val player = getPlayer()
        _uiState.value = _uiState.value.copy(
            currentChannel = channel,
            status = PlaybackStatus.BUFFERING,
            errorMessage = null,
            videoResolution = ""
        )

        try {
            val uri = Uri.parse(channel.streamUrl.trim())
            val mediaItemBuilder = MediaItem.Builder().setUri(uri)

            val mediaSource = if (channel.streamUrl.contains(".m3u8", ignoreCase = true)) {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItemBuilder.build())
            } else if (channel.streamUrl.contains(".mp4", ignoreCase = true)) {
                mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MP4)
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItemBuilder.build())
            } else {
                DefaultMediaSourceFactory(httpDataSourceFactory).createMediaSource(mediaItemBuilder.build())
            }

            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                status = PlaybackStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Invalid stream URL"
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false, status = PlaybackStatus.PAUSED)
        } else {
            player.play()
            _uiState.value = _uiState.value.copy(isPlaying = true, status = PlaybackStatus.PLAYING)
        }
    }

    fun toggleMute() {
        val player = exoPlayer ?: return
        val currentMuted = _uiState.value.isMuted
        val newMuted = !currentMuted
        player.volume = if (newMuted) 0f else 1f
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
    }

    fun cycleAspectRatio(): AspectRatioMode {
        val modes = AspectRatioMode.values()
        val currentIdx = modes.indexOf(_uiState.value.aspectRatioMode)
        val nextMode = modes[(currentIdx + 1) % modes.size]
        _uiState.value = _uiState.value.copy(aspectRatioMode = nextMode)
        return nextMode
    }

    fun retryCurrentStream() {
        val channel = _uiState.value.currentChannel
        if (channel != null) {
            playChannel(channel)
        }
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
