package com.daymark.app.features.main.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daymark.app.core.designsystem.LocalDaymarkColors
import com.daymark.app.features.main.MainTab

@Composable
fun DaymarkTopBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App branding (minimalist wordmark)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Daymark",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    color = colors.textPrimary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ChatGPT-inspired Animated Pill Switch
        DaymarkSegmentedSwitch(
            currentTab = currentTab,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Settings Icon immediately beside the switch
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DaymarkSegmentedSwitch(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDaymarkColors.current
    val tabs = listOf(MainTab.TODO to "Todo", MainTab.GOALS to "Goals")
    val selectedIndex = if (currentTab == MainTab.TODO) 0 else 1

    BoxWithConstraints(
        modifier = modifier
            .height(36.dp)
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.secondaryContainer)
            .padding(3.dp)
    ) {
        val tabWidth = (maxWidth) / 2
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = 280),
            label = "pill_slider"
        )

        // Sliding active pill background
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(15.dp))
                .background(colors.primaryBackground)
        )

        // Tab Options
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, (tab, title) ->
                val isSelected = currentTab == tab
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(tab)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary
                        )
                    )
                }
            }
        }
    }
}
