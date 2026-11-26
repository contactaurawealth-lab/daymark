package com.daymark.app.features.goals.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.daymark.app.core.designsystem.LocalDaymarkColors

@Composable
fun GoalActiveLimitDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalDaymarkColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.primaryBackground,
        title = {
            Text(
                text = "Focus Limit Reached",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
        },
        text = {
            Text(
                text = "Daymark enforces a strict maximum of 6 active goals.\n\nDeep focus requires restraint: doing fewer things consistently produces lasting momentum.\n\nTo start a new goal, archive or complete one of your current active goals.",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Got It", color = Color.White)
            }
        }
    )
}
