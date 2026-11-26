package com.daymark.app.core.designsystem

import com.daymark.app.domain.models.AppTheme

object ThemePresets {
    val Daylight = AppTheme(
        id = "daylight",
        name = "Daylight",
        primaryColor = 0xFFFBFBF9,
        secondaryColor = 0xFFF0EFEB,
        accentColor = 0xFF91A68A,
        isDark = false
    )

    val Sandstone = AppTheme(
        id = "sandstone",
        name = "Sandstone",
        primaryColor = 0xFFF6F3EE,
        secondaryColor = 0xFFE8E2D5,
        accentColor = 0xFFA76D38,
        isDark = false
    )

    val Midnight = AppTheme(
        id = "midnight",
        name = "Midnight",
        primaryColor = 0xFF0F1115,
        secondaryColor = 0xFF1E232B,
        accentColor = 0xFF38BDF8,
        isDark = true
    )

    val Slate = AppTheme(
        id = "slate",
        name = "Slate",
        primaryColor = 0xFF1E222A,
        secondaryColor = 0xFF2C3340,
        accentColor = 0xFF60A5FA,
        isDark = true
    )

    val Rose = AppTheme(
        id = "rose",
        name = "Rose",
        primaryColor = 0xFFFAF5F5,
        secondaryColor = 0xFFEFE3E3,
        accentColor = 0xFFBE4B5F,
        isDark = false
    )

    val allPresets = listOf(Daylight, Sandstone, Midnight, Slate, Rose)

    fun getById(id: String): AppTheme {
        return allPresets.find { it.id == id } ?: Daylight
    }
}
