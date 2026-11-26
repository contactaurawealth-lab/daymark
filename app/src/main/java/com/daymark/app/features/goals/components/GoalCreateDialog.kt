package com.daymark.app.features.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.domain.models.TrackingType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalCreateDialog(
    onConfirm: (
        title: String,
        description: String?,
        icon: String,
        color: Long,
        trackingType: TrackingType,
        targetValue: Double,
        unit: String,
        durationDays: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var trackingType by remember { mutableStateOf(TrackingType.CHECKBOX) }
    var targetValueText by remember { mutableStateOf("10") }
    var unitText by remember { mutableStateOf("pages") }

    val durationPresets = listOf(7, 14, 21, 30, 60, 90)
    var selectedDuration by remember { mutableStateOf(30) }

    val icons = listOf("sparkles", "book", "fitness", "water", "meditation", "code", "walk", "sleep")
    var selectedIcon by remember { mutableStateOf("sparkles") }

    val colorPalette = listOf(
        0xFF91A68A, // Sage
        0xFFA76D38, // Terracotta
        0xFF38BDF8, // Frost Blue
        0xFFBE4B5F, // Rose
        0xFF818CF8, // Lavender
        0xFFF59E0B  // Amber
    )
    var selectedColor by remember { mutableStateOf(colorPalette[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.primaryBackground,
        title = {
            Text(
                text = "New Goal",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Name (e.g. Read daily)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Motivation / notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Completion Method Switcher
                Text(
                    text = "Completion Method",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, color = colors.textSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (trackingType == TrackingType.CHECKBOX) colors.accent else colors.secondaryContainer)
                            .clickable { trackingType = TrackingType.CHECKBOX }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Checkbox",
                            color = if (trackingType == TrackingType.CHECKBOX) Color.White else colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (trackingType == TrackingType.MEASURABLE) colors.accent else colors.secondaryContainer)
                            .clickable { trackingType = TrackingType.MEASURABLE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Measurable",
                            color = if (trackingType == TrackingType.MEASURABLE) Color.White else colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                if (trackingType == TrackingType.MEASURABLE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetValueText,
                            onValueChange = { targetValueText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Target") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = unitText,
                            onValueChange = { unitText = it },
                            label = { Text("Unit (pages, km)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Duration Presets
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, color = colors.textSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durationPresets.forEach { days ->
                        val isSel = selectedDuration == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) colors.accent else colors.secondaryContainer)
                                .clickable { selectedDuration = days }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$days d",
                                color = if (isSel) Color.White else colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon Picker
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, color = colors.textSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.forEach { iconKey ->
                        val isSel = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSel) colors.accent.copy(alpha = 0.3f) else colors.secondaryContainer)
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = getIconEmoji(iconKey), fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Palette
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp, color = colors.textSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorPalette.forEach { colorVal ->
                        val isSel = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = targetValueText.toDoubleOrNull()?.coerceAtLeast(0.1) ?: 1.0
                        onConfirm(
                            title.trim(),
                            description.trim().ifBlank { null },
                            selectedIcon,
                            selectedColor,
                            trackingType,
                            target,
                            unitText.trim().ifBlank { "times" },
                            selectedDuration
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Create Goal", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}
