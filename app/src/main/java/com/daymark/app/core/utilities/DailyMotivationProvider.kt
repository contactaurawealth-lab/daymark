package com.daymark.app.core.utilities

import com.daymark.app.domain.models.MotivationalMessage
import java.time.LocalDate
import java.time.LocalTime

object DailyMotivationProvider {

    private val quotes = listOf(
        "We are what we repeatedly do. Excellence, then, is not an act, but a habit." to "Will Durant",
        "The secret of getting ahead is getting started." to "Mark Twain",
        "It is not daily increase, but daily decrease. Hack away at the unessential." to "Bruce Lee",
        "Focus is a muscle. The more you protect your attention, the stronger it grows." to "Cal Newport",
        "You do not rise to the level of your goals. You fall to the level of your systems." to "James Clear",
        "Simplicity is the prerequisite for reliability." to "Edsger W. Dijkstra",
        "Do not spoil what you have by desiring what you have not." to "Epicurus",
        "Small disciplines repeated with consistency every day lead to great achievements." to "John C. Maxwell",
        "He who has a why to live can bear almost any how." to "Friedrich Nietzsche",
        "The mind is everything. What you think you become." to "Buddha",
        "Concentrate all your thoughts upon the work at hand. The sun's rays do not burn until brought to a focus." to "Alexander Graham Bell",
        "Action is the foundational key to all success." to "Pablo Picasso",
        "It always seems impossible until it is done." to "Nelson Mandela",
        "Peace comes from within. Do not seek it without." to "Gautama Buddha",
        "Waste no more time arguing what a good man should be. Be one." to "Marcus Aurelius",
        "Continuous improvement is better than delayed perfection." to "Mark Twain",
        "Do what you can, with what you have, where you are." to "Theodore Roosevelt",
        "Very little is needed to make a happy life; it is all within yourself." to "Marcus Aurelius",
        "Clarity of mind means clarity of passion, too; this is why a great and clear mind loves ardently." to "Blaise Pascal",
        "The journey of a thousand miles begins with a single step." to "Lao Tzu",
        "Simplicity boils down to two steps: Identify the essential. Eliminate the rest." to "Leo Babauta",
        "Well begun is half done." to "Aristotle",
        "Nature does not hurry, yet everything is accomplished." to "Lao Tzu",
        "The soul becomes dyed with the color of its thoughts." to "Marcus Aurelius",
        "Effort only fully releases its reward after a person refuses to quit." to "Napoleon Hill",
        "Make each day your masterpiece." to "John Wooden",
        "To be calm is the highest achievement of the self." to "Zen Proverb",
        "One day or day one. You decide." to "Unknown",
        "Happiness is not something ready made. It comes from your own actions." to "Dalai Lama",
        "Do less, with more focus." to "Daymark Principle"
    )

    private val reflectionPrompts = listOf(
        "What is the single most meaningful mark you can make today?",
        "If you could complete only one task before nightfall, which one matters most?",
        "Breathe. Remove one non-essential expectation from your mind.",
        "Notice what is already completed before worrying about what remains.",
        "Progress is rarely loud. Value the quiet consistency of small acts.",
        "Reflect on what gave you energy today, and what drained it.",
        "Close your tasks with kindness. Tomorrow is a clean slate."
    )

    private val completionEncouragements = listOf(
        "Clearly marked.",
        "Quiet progress.",
        "Momentum built.",
        "Done with intention.",
        "One step forward.",
        "Focus preserved.",
        "A mark well made."
    )

    fun getTodayMessage(localDate: LocalDate = LocalDate.now()): MotivationalMessage {
        val dayIndex = (localDate.dayOfYear - 1).coerceAtLeast(0)
        val (quote, author) = quotes[dayIndex % quotes.size]
        val prompt = reflectionPrompts[dayIndex % reflectionPrompts.size]

        val hour = LocalTime.now().hour
        val greeting = when (hour) {
            in 5..11 -> "Good morning. Mark this day with clarity."
            in 12..17 -> "Good afternoon. Keep your focus steady."
            in 18..21 -> "Good evening. Review your day with calm."
            else -> "Night hours. Rest and prepare for tomorrow."
        }

        return MotivationalMessage(
            quote = quote,
            author = author,
            timeOfDayGreeting = greeting,
            reflectionPrompt = prompt
        )
    }

    fun getRandomCompletionMessage(): String {
        return completionEncouragements.random()
    }
}
