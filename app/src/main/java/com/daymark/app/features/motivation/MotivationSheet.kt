package com.daymark.app.features.motivation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.core.utilities.DailyMotivationProvider
import com.daymark.app.domain.models.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationSheet(
    settings: AppSettings,
    onSaveVideoUri: (String?) -> Unit,
    onSetStartFromBeginning: (Boolean) -> Unit,
    onSaveResumePosition: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val message = remember { DailyMotivationProvider.getTodayMessage() }
    var isPlayingVideo by rememberSaveable { mutableStateOf(false) }

    // Media picker launcher with persistable permission
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported
            }
            onSaveVideoUri(uri.toString())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.primaryBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Motivation",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting & Time-of-day reflection
            if (settings.morningGreetingEnabled || settings.eveningReflectionEnabled) {
                Text(
                    text = message.timeOfDayGreeting,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.reflectionPrompt,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Daily Quote Card
            if (settings.dailyMotivationEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.secondaryContainer)
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "\"${message.quote}\"",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                lineHeight = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— ${message.author}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            ),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(18.dp))

            // SECTION: PERSONAL VIDEO MOTIVATION
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.VideoLibrary,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personal Video Motivation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keep a personal video that reminds you why you started — a message to yourself, a mentor's advice, or a proud memory.",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentVideo = settings.customVideoUri

            if (isPlayingVideo && currentVideo != null) {
                // Active video playback
                PersonalVideoPlayer(
                    videoUriString = currentVideo,
                    startFromBeginning = settings.videoStartFromBeginning,
                    savedResumePositionMs = settings.videoResumePositionMs,
                    onSaveResumePosition = onSaveResumePosition,
                    onClosePlayer = { isPlayingVideo = false }
                )
            } else if (currentVideo != null) {
                // Prominent Play Button
                Button(
                    onClick = { isPlayingVideo = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Personal Motivation",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Upload, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Replace Video", color = colors.textPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSaveVideoUri(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove Video", color = Color(0xFFE53935), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback mode option (start from beginning vs resume)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.secondaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start from beginning",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        )
                        Text(
                            text = if (settings.videoStartFromBeginning) "Plays from 0:00 every time" else "Resumes from last position",
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                        )
                    }

                    Switch(
                        checked = settings.videoStartFromBeginning,
                        onCheckedChange = onSetStartFromBeginning,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.accent
                        )
                    )
                }
            } else {
                // Empty state when no video is selected yet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.secondaryContainer.copy(alpha = 0.5f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.VideoLibrary, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No motivation video selected",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload a video from your gallery to play whenever you need a push.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Icon(Icons.Outlined.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Video from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
