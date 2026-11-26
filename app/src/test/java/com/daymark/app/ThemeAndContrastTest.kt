package com.daymark.app

import com.daymark.app.core.designsystem.ThemePresets
import com.daymark.app.core.designsystem.calculateContrastTextColor
import com.daymark.app.core.designsystem.calculateSecondaryTextColor
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeAndContrastTest {

    @Test
    fun testPresetsCountAndAttributes() {
        val presets = ThemePresets.allPresets
        assertEquals(5, presets.size)

        val daylight = ThemePresets.getById("daylight")
        assertEquals("Daylight", daylight.name)
        assertEquals(0xFFFBFBF9, daylight.primaryColor)
        assertEquals(0xFF91A68A, daylight.accentColor)

        val sandstone = ThemePresets.getById("sandstone")
        assertEquals("Sandstone", sandstone.name)

        val midnight = ThemePresets.getById("midnight")
        assertEquals(true, midnight.isDark)

        val slate = ThemePresets.getById("slate")
        assertEquals(true, slate.isDark)

        val rose = ThemePresets.getById("rose")
        assertEquals("Rose", rose.name)
    }

    @Test
    fun testContrastTextColorForLightBackground() {
        // Pure White / Light Ivory
        val lightColor = 0xFFFFFFFF
        val textOnLight = calculateContrastTextColor(lightColor)
        // Should be dark text
        assertEquals(Color(0xFF1E222A), textOnLight)
    }

    @Test
    fun testContrastTextColorForDarkBackground() {
        // Deep obsidian
        val darkColor = 0xFF0F1115
        val textOnDark = calculateContrastTextColor(darkColor)
        // Should be light text
        assertEquals(Color(0xFFF9FAFB), textOnDark)
    }
}
