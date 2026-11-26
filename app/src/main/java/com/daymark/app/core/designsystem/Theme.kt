package com.daymark.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.daymark.app.domain.models.AppTheme
import com.daymark.app.domain.models.ThemeMode

/**
 * 60:30:10 Design Tokens
 * 60% = primaryBackground (surface/background)
 * 30% = secondaryContainer (cards, dividers, segmented switches)
 * 10% = accent (key buttons, check marks, active highlights)
 */
data class DaymarkColors(
    val primaryBackground: Color,
    val secondaryContainer: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val isDark: Boolean
)

val LocalDaymarkColors = staticCompositionLocalOf {
    DaymarkColors(
        primaryBackground = DaylightPrimary,
        secondaryContainer = DaylightSecondary,
        accent = DaylightAccent,
        textPrimary = Color(0xFF2B303A),
        textSecondary = Color(0xFF71747C),
        divider = Color(0xFFE2E0D8),
        isDark = false
    )
}

fun calculateContrastTextColor(backgroundHex: Long): Color {
    val r = ((backgroundHex shr 16) and 0xFF) / 255.0
    val g = ((backgroundHex shr 8) and 0xFF) / 255.0
    val b = (backgroundHex and 0xFF) / 255.0
    // Relative luminance
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return if (luminance < 0.5) Color(0xFFF9FAFB) else Color(0xFF1E222A)
}

fun calculateSecondaryTextColor(backgroundHex: Long): Color {
    val r = ((backgroundHex shr 16) and 0xFF) / 255.0
    val g = ((backgroundHex shr 8) and 0xFF) / 255.0
    val b = (backgroundHex and 0xFF) / 255.0
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return if (luminance < 0.5) Color(0xFF9CA3AF) else Color(0xFF6B7280)
}

fun buildDaymarkColorScheme(appTheme: AppTheme, isDark: Boolean): ColorScheme {
    val bg = Color(appTheme.primaryColor)
    val container = Color(appTheme.secondaryColor)
    val acc = Color(appTheme.accentColor)
    val textPri = calculateContrastTextColor(appTheme.primaryColor)
    val textSec = calculateSecondaryTextColor(appTheme.primaryColor)

    return if (isDark) {
        darkColorScheme(
            primary = acc,
            onPrimary = calculateContrastTextColor(appTheme.accentColor),
            primaryContainer = container,
            onPrimaryContainer = textPri,
            secondary = container,
            onSecondary = textPri,
            background = bg,
            onBackground = textPri,
            surface = bg,
            onSurface = textPri,
            surfaceVariant = container,
            onSurfaceVariant = textSec,
            outline = textSec.copy(alpha = 0.3f)
        )
    } else {
        lightColorScheme(
            primary = acc,
            onPrimary = calculateContrastTextColor(appTheme.accentColor),
            primaryContainer = container,
            onPrimaryContainer = textPri,
            secondary = container,
            onSecondary = textPri,
            background = bg,
            onBackground = textPri,
            surface = bg,
            onSurface = textPri,
            surfaceVariant = container,
            onSurfaceVariant = textSec,
            outline = textSec.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun DaymarkTheme(
    appTheme: AppTheme = ThemePresets.Daylight,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> appTheme.isDark || systemInDark
    }

    val customColors = DaymarkColors(
        primaryBackground = Color(appTheme.primaryColor),
        secondaryContainer = Color(appTheme.secondaryColor),
        accent = Color(appTheme.accentColor),
        textPrimary = calculateContrastTextColor(appTheme.primaryColor),
        textSecondary = calculateSecondaryTextColor(appTheme.primaryColor),
        divider = if (isDark) Color(0xFF2E3440) else Color(0xFFE2E0D8),
        isDark = isDark
    )

    val colorScheme = buildDaymarkColorScheme(appTheme, isDark)

    CompositionLocalProvider(LocalDaymarkColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
