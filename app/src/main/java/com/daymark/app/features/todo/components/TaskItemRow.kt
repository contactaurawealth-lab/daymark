package com.daymark.app.features.todo.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.core.utilities.DateTimeUtils
import com.daymark.app.domain.models.Priority
import com.daymark.app.domain.models.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItemRow(
    task: Task,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    onStartTimer: () -> Unit,
    onEdit: () -> Unit,
    onSetReminder: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    val checkboxBg by animateColorAsState(
        targetValue = if (task.completed) colors.accent else Color.Transparent,
        label = "checkbox_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.secondaryContainer.copy(alpha = if (task.completed) 0.4f else 0.7f))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Accessible Checkbox
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleCompletion),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(checkboxBg),
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.primaryBackground)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title, description & badges
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.completed) colors.textSecondary else colors.textPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags row (Due date, Reminder, Priority)
            if (task.dueDate != null || task.reminderTime != null || task.priority == Priority.HIGH) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority == Priority.HIGH) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (task.dueDate != null) {
                        Text(
                            text = DateTimeUtils.formatRelativeDate(task.dueDate),
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (task.reminderTime != null) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Reminder set",
                            tint = colors.accent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Contextual menu button (accessible alternative to long-press)
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Task Options",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Contextual Menu
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(colors.primaryBackground)
            ) {
                DropdownMenuItem(
                    text = { Text("Start Timer", color = colors.textPrimary) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = colors.accent)
                    },
                    onClick = {
                        menuExpanded = false
                        onStartTimer()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit Task", color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Set Reminder", color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onSetReminder()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate", color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onDuplicate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFE53935)) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
