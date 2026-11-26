package com.daymark.app.core.utilities

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    private val zoneId: ZoneId get() = ZoneId.systemDefault()

    fun todayEpochDay(): Long {
        return LocalDate.now(zoneId).toEpochDay()
    }

    fun epochDayToLocalDate(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }

    fun formatTodayHeader(): String {
        val now = LocalDate.now(zoneId)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
        return now.format(formatter)
    }

    fun formatDate(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        return date.format(formatter)
    }

    fun formatRelativeDate(epochDay: Long): String {
        val today = todayEpochDay()
        return when (epochDay) {
            today -> "Today"
            today + 1 -> "Tomorrow"
            today - 1 -> "Yesterday"
            else -> formatDate(epochDay)
        }
    }

    fun formatTime(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(zoneId)
        return formatter.format(instant)
    }

    fun formatSecondsToTime(seconds: Long): String {
        val safeSeconds = if (seconds < 0) 0 else seconds
        val hrs = safeSeconds / 3600
        val mins = (safeSeconds % 3600) / 60
        val secs = safeSeconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }
}
