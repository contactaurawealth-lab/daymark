package com.daymark.app

import com.daymark.app.core.utilities.DailyMotivationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyMotivationProviderTest {

    @Test
    fun testDailyMessageContainsContent() {
        val msg = DailyMotivationProvider.getTodayMessage()
        assertNotNull(msg)
        assertTrue(msg.quote.isNotBlank())
        assertTrue(msg.author.isNotBlank())
        assertTrue(msg.timeOfDayGreeting.isNotBlank())
        assertTrue(msg.reflectionPrompt.isNotBlank())
    }

    @Test
    fun testEncouragingCompletionMessage() {
        val toast = DailyMotivationProvider.getRandomCompletionMessage()
        assertNotNull(toast)
        assertTrue(toast.isNotBlank())
    }

    @Test
    fun testMessageRotationDeterminism() {
        val date1 = LocalDate.of(2026, 11, 25)
        val msg1 = DailyMotivationProvider.getTodayMessage(date1)
        val msg2 = DailyMotivationProvider.getTodayMessage(date1)
        assertEquals(msg1.quote, msg2.quote)
        assertEquals(msg1.author, msg2.author)

        val nextDate = date1.plusDays(1)
        val msgNext = DailyMotivationProvider.getTodayMessage(nextDate)
        assertNotNull(msgNext)
    }
}
