package com.daymark.app.features.motivation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.daymark.app.R
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.core.utilities.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun computeVideoAspectRatio(
    width: Int,
    height: Int,
    pixelWidthHeightRatio: Float = 1f
): Float? {
    if (width <= 0 || height <= 0) return null
    val ratio = (width * pixelWidthHeightRatio) / height.toFloat()
    return if (ratio.isFinite() && ratio > 0f) ratio else null
}

internal fun shouldMaximizeInLandscape(aspectRatio: Float): Boolean = aspectRatio > 1.2f

@OptIn(UnstableApi::class)
@Composable
fun PersonalVideoPlayer(
    videoUriString: String,
    startFromBeginning: Boolean = true,
    savedResumePositionMs: Long = 0L,
    onSaveResumePosition: (Long) -> Unit,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val colors = LocalDaymarkColors.current

    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var videoAspectRatio by remember(videoUriString) { mutableFloatStateOf(16f / 9f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    val exoPlayer = remember(videoUriString) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Match orientation smartly when maximized and restore on exit
    DisposableEffect(isFullscreen, videoAspectRatio) {
        if (isFullscreen && activity != null) {
            val previousOrientation = activity.requestedOrientation
            activity.requestedOrientation = if (shouldMaximizeInLandscape(videoAspectRatio)) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            onDispose {
                activity.requestedOrientation = previousOrientation
            }
        } else {
            onDispose {}
        }
    }

    DisposableEffect(videoUriString) {
        try {
            val uri = Uri.parse(videoUriString)
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            val initialPosition = if (startFromBeginning) 0L else savedResumePositionMs
            if (initialPosition > 0L) {
                exoPlayer.seekTo(initialPosition)
            }

            val listener = object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    computeVideoAspectRatio(
                        width = videoSize.width,
                        height = videoSize.height,
                        pixelWidthHeightRatio = videoSize.pixelWidthHeightRatio
                    )?.let { ratio ->
                        videoAspectRatio = ratio
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        errorMessage = null
                    } else if (playbackState == Player.STATE_ENDED) {
                        isPlaying = false
                        onSaveResumePosition(0L)
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlayerError(error: PlaybackException) {
                    errorMessage = "Video file not found or inaccessible. Please re-select the video."
                }
            }
            exoPlayer.addListener(listener)

            onDispose {
                exoPlayer.removeListener(listener)
                onSaveResumePosition(exoPlayer.currentPosition)
                exoPlayer.release()
            }
        } catch (e: Exception) {
            errorMessage = "Unable to open video: ${e.message}"
            onDispose {
                exoPlayer.release()
            }
        }
    }

    // Position progress tracking ticker
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (!isUserSeeking) {
                currentPositionMs = exoPlayer.currentPosition
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
                if (durationMs > 0L) {
                    seekProgress = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                }
            }
            delay(250)
        }
    }

    if (errorMessage != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Video Inaccessible",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClosePlayer,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Close Player", color = Color.White)
                }
            }
        }
        return
    }

    if (isFullscreen) {
        // Placeholder inside bottom sheet while maximized in Dialog
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { isFullscreen = false },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Playing in Fullscreen • Tap to Minimize", color = Color.White)
            }
        }

        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            VideoPlayerViewport(
                exoPlayer = exoPlayer,
                isFullscreen = true,
                showControls = showControls,
                onToggleControls = { showControls = !showControls },
                isPlaying = isPlaying,
                isMuted = isMuted,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                seekProgress = seekProgress,
                onSeekStart = {
                    isUserSeeking = true
                    seekProgress = it
                },
                onSeekFinish = {
                    isUserSeeking = false
                    val targetMs = (seekProgress * durationMs).toLong()
                    exoPlayer.seekTo(targetMs)
                    currentPositionMs = targetMs
                },
                onTogglePlayPause = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                        }
                        exoPlayer.play()
                    }
                },
                onToggleMute = {
                    isMuted = !isMuted
                    exoPlayer.volume = if (isMuted) 0f else 1f
                },
                onToggleFullscreen = { isFullscreen = false },
                onClosePlayer = {
                    isFullscreen = false
                    onClosePlayer()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    } else {
        // Dynamic aspect ratio sizing for inline playback (supports portrait, square, landscape, ultrawide)
        val inlineBoxModifier = if (videoAspectRatio >= 1f) {
            modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .aspectRatio(videoAspectRatio.coerceIn(1f, 2.35f))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
        } else {
            modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
        }

        VideoPlayerViewport(
            exoPlayer = exoPlayer,
            isFullscreen = false,
            showControls = showControls,
            onToggleControls = { showControls = !showControls },
            isPlaying = isPlaying,
            isMuted = isMuted,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            seekProgress = seekProgress,
            onSeekStart = {
                isUserSeeking = true
                seekProgress = it
            },
            onSeekFinish = {
                isUserSeeking = false
                val targetMs = (seekProgress * durationMs).toLong()
                exoPlayer.seekTo(targetMs)
                currentPositionMs = targetMs
            },
            onTogglePlayPause = {
                if (isPlaying) {
                    exoPlayer.pause()
                } else {
                    if (exoPlayer.playbackState == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                }
            },
            onToggleMute = {
                isMuted = !isMuted
                exoPlayer.volume = if (isMuted) 0f else 1f
            },
            onToggleFullscreen = { isFullscreen = true },
            onClosePlayer = onClosePlayer,
            modifier = inlineBoxModifier
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerViewport(
    exoPlayer: ExoPlayer,
    isFullscreen: Boolean,
    showControls: Boolean,
    onToggleControls: () -> Unit,
    isPlaying: Boolean,
    isMuted: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    seekProgress: Float,
    onSeekStart: (Float) -> Unit,
    onSeekFinish: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onToggleControls
        ),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                (LayoutInflater.from(ctx).inflate(
                    R.layout.view_personal_video_player,
                    null,
                    false
                ) as PlayerView).apply {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useController = false
                    player = exoPlayer
                }
            },
            update = { view ->
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
            },
            onRelease = { view ->
                view.player = null
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .then(
                        if (isFullscreen) Modifier.navigationBarsPadding() else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateTimeUtils.formatSecondsToTime(currentPositionMs / 1000),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                    )

                    Slider(
                        value = seekProgress,
                        onValueChange = onSeekStart,
                        onValueChangeFinished = onSeekFinish,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Text(
                        text = DateTimeUtils.formatSecondsToTime(durationMs / 1000),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) {
                                    Icons.AutoMirrored.Outlined.VolumeMute
                                } else {
                                    Icons.AutoMirrored.Outlined.VolumeUp
                                },
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) {
                                    Icons.Outlined.FullscreenExit
                                } else {
                                    Icons.Outlined.Fullscreen
                                },
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Maximize",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Player",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
