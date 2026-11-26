package com.daymark.app.features.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.core.utilities.AmbientSoundPlayer
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.TimerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerBottomSheet(
    viewModel: TimerViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current
    val timerState by viewModel.timerState.collectAsState()
    val remainingSec by viewModel.remainingSeconds.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var ambientSoundOn by remember { mutableStateOf(AmbientSoundPlayer.isPlaying) }

    androidx.activity.compose.BackHandler(enabled = true) {
        onDismiss()
    }

    val progressFraction = if (timerState.totalDurationSeconds > 0) {
        (remainingSec.toFloat() / timerState.totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "timer_circle_prog")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primaryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Session",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textSecondary,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Minimize", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = timerState.taskTitle.ifBlank { "Task Focus" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )

            // Pomodoro Mode Quick Selector
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Standard (25m)" to 25,
                    "Short (15m)" to 15,
                    "Deep (45m)" to 45,
                    "Break (5m)" to 5
                ).forEach { (label, duration) ->
                    val isSel = timerState.totalDurationSeconds == (duration * 60L)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) colors.accent else colors.secondaryContainer.copy(alpha = 0.6f))
                            .clickable {
                                if (timerState.status == TimerStatus.IDLE || timerState.status == TimerStatus.COMPLETED) {
                                    viewModel.startTimer(null, duration)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSel) Color.White else colors.textPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Circular Countdown Display
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 10.dp,
                    color = colors.secondaryContainer
                )

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 10.dp,
                    color = colors.accent
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DateTimeUtils.formatSecondsToTime(remainingSec),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (timerState.status) {
                            TimerStatus.RUNNING -> "Focusing"
                            TimerStatus.PAUSED -> "Paused"
                            TimerStatus.COMPLETED -> "Finished"
                            TimerStatus.IDLE -> "Ready"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Controls (Pause/Resume, Cancel/Stop, Ambient Sound)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel / Stop button
                IconButton(
                    onClick = {
                        viewModel.cancelTimer()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = "Stop Timer",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Pause / Resume Main Action
                val isPaused = timerState.status == TimerStatus.PAUSED
                IconButton(
                    onClick = {
                        if (isPaused) viewModel.resumeTimer() else viewModel.pauseTimer()
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Ambient Sound Toggle
                IconButton(
                    onClick = {
                        ambientSoundOn = AmbientSoundPlayer.toggle()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (ambientSoundOn) colors.accent.copy(alpha = 0.2f) else colors.secondaryContainer)
                ) {
                    Icon(
                        imageVector = if (ambientSoundOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                        contentDescription = "Ambient Sound",
                        tint = if (ambientSoundOn) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
