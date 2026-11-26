package com.daymark.app

import com.daymark.app.features.motivation.computeVideoAspectRatio
import com.daymark.app.features.motivation.shouldMaximizeInLandscape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoDimensionTest {

    @Test
    fun testPortraitSquareLandscapeAndUltrawideAspectRatios() {
        // 9:16 portrait video (e.g. phone vertical)
        val portrait = computeVideoAspectRatio(1080, 1920)!!
        assertEquals(0.5625f, portrait, 0.001f)
        assertFalse(shouldMaximizeInLandscape(portrait))

        // 1:1 square video
        val square = computeVideoAspectRatio(1080, 1080)!!
        assertEquals(1.0f, square, 0.001f)
        assertFalse(shouldMaximizeInLandscape(square))

        // 4:3 classic video
        val fourByThree = computeVideoAspectRatio(1440, 1080)!!
        assertEquals(1.333f, fourByThree, 0.005f)
        assertTrue(shouldMaximizeInLandscape(fourByThree))

        // 16:9 widescreen video
        val widescreen = computeVideoAspectRatio(1920, 1080)!!
        assertEquals(1.778f, widescreen, 0.005f)
        assertTrue(shouldMaximizeInLandscape(widescreen))

        // 21:9 ultrawide video
        val ultrawide = computeVideoAspectRatio(2560, 1080)!!
        assertEquals(2.370f, ultrawide, 0.005f)
        assertTrue(shouldMaximizeInLandscape(ultrawide))
    }

    @Test
    fun testAnamorphicPixelAspectRatioAndInvalidDimensions() {
        val ratio = computeVideoAspectRatio(1440, 1080, pixelWidthHeightRatio = 1.3333334f)!!
        assertEquals(1.778f, ratio, 0.01f)

        assertNull(computeVideoAspectRatio(0, 1080))
        assertNull(computeVideoAspectRatio(1920, 0))
        assertNull(computeVideoAspectRatio(-100, 100))
    }
}
