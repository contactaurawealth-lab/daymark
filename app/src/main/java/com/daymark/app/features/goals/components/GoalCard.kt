package com.daymark.app.features.goals.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.TrackingType
import com.daymark.app.features.goals.GoalItemState

@Composable
fun GoalCard(
    item: GoalItemState,
    onClick: () -> Unit,
    onToggleCheckbox: () -> Unit,
    onAddMeasurableValue: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current
    val goal = item.goal
    val streak = item.streakInfo
    val isCompleted = item.todayCompleted

    val goalAccent = Color(goal.color)

    val checkboxBg by animateColorAsState(
        targetValue = if (isCompleted) goalAccent else Color.Transparent,
        label = "goal_check_bg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.secondaryContainer.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(goalAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getIconEmoji(goal.icon),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streak.currentStreak > 0) {
                        Text(
                            text = "🔥 ${streak.currentStreak} day streak",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.accent,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "${streak.remainingDays}d left",
                        style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                    )
                }
            }

            // Completion Action (Checkbox or Measurable Quick Add)
            if (goal.trackingType == TrackingType.CHECKBOX) {
                IconButton(
                    onClick = onToggleCheckbox,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(checkboxBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed Today",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryBackground)
                            )
                        }
                    }
                }
            } else {
                // Measurable Goal Quick Add (+1)
                IconButton(
                    onClick = { onAddMeasurableValue(1.0) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) goalAccent else colors.primaryBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Add progress",
                            tint = if (isCompleted) Color.White else colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Measurable progress bar & numbers
        if (goal.trackingType == TrackingType.MEASURABLE) {
            Spacer(modifier = Modifier.height(12.dp))

            val progressFraction = (item.todayProgress / goal.targetValue).coerceIn(0.0, 1.0).toFloat()
            val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "meas_progress")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = goalAccent,
                trackColor = colors.primaryBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val formattedProgress = if (item.todayProgress % 1.0 == 0.0) item.todayProgress.toInt().toString() else item.todayProgress.toString()
                val formattedTarget = if (goal.targetValue % 1.0 == 0.0) goal.targetValue.toInt().toString() else goal.targetValue.toString()

                Text(
                    text = "$formattedProgress / $formattedTarget ${goal.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isCompleted) goalAccent else colors.textSecondary,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

fun getIconEmoji(iconKey: String): String {
    return when (iconKey.lowercase()) {
        "fitness", "workout" -> "🏃"
        "book", "read" -> "📖"
        "water", "drink" -> "💧"
        "meditation", "calm" -> "🧘"
        "code", "dev" -> "💻"
        "walk" -> "🚶"
        "sleep" -> "🌙"
        "heart", "health" -> "❤️"
        else -> "✨"
    }
}
