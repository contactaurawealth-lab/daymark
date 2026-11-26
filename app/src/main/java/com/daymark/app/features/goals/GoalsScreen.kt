package com.daymark.app.features.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.features.goals.components.GoalActiveLimitDialog
import com.daymark.app.features.goals.components.GoalCard
import com.daymark.app.features.goals.components.GoalCreateDialog
import com.daymark.app.features.goals.components.GoalDetailSheet

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalDaymarkColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.primaryBackground)
            .padding(horizontal = 20.dp)
    ) {
        // Goals Header with Active Count (e.g. 3 / 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
                Text(
                    text = "${uiState.activeGoalsCount} of 6 active goals",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onRequestAddGoal() },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Goal", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.activeGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active goals",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick 3 to 6 daily habits that truly matter.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(uiState.activeGoals, key = { it.goal.id }) { item ->
                    GoalCard(
                        item = item,
                        onClick = { viewModel.openGoalDetail(item) },
                        onToggleCheckbox = { viewModel.toggleCheckboxGoal(item.goal.id) },
                        onAddMeasurableValue = { amount -> viewModel.recordMeasurableProgress(item.goal, amount) }
                    )
                }
            }
        }
    }

    // Dialogs & Sheets
    if (uiState.showCreateDialog) {
        GoalCreateDialog(
            onConfirm = { title, desc, icon, col, type, target, unit, dur ->
                viewModel.createGoal(title, desc, icon, col, type, target, unit, dur)
            },
            onDismiss = { viewModel.dismissCreateDialog() }
        )
    }

    if (uiState.showLimitReachedDialog) {
        GoalActiveLimitDialog(onDismiss = { viewModel.dismissLimitDialog() })
    }

    uiState.selectedGoalDetail?.let { detail ->
        GoalDetailSheet(
            item = detail,
            onArchive = { viewModel.archiveGoal(detail.goal.id) },
            onDelete = { viewModel.deleteGoal(detail.goal.id) },
            onDismiss = { viewModel.closeGoalDetail() }
        )
    }
}
