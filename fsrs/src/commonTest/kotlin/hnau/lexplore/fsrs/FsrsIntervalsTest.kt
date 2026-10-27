package hnau.lexplore.fsrs

import hnau.lexplore.fsrs.dto.Difficulty
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.Stability
import hnau.lexplore.fsrs.dto.WordInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FsrsIntervalsTest {

    private val start = Instant.fromEpochMilliseconds(0)

    private data class Review(
        val timeMinutes: Double,
        val stabilityMinutes: Double,
        val difficulty: Double,
    )

    private val Stability.minutes: Double
        get() = duration / 1.minutes

    private fun simulate(
        firstRating: FsrsRating,
        nextRatings: List<FsrsRating>,
    ): List<Review> {
        var info = Fsrs.initialState(
            rating = firstRating,
            now = start,
        )
        var now = start
        val reviews = mutableListOf(
            Review(
                timeMinutes = 0.0,
                stabilityMinutes = info.stability.minutes,
                difficulty = info.difficulty.value,
            )
        )
        nextRatings.forEach { rating ->
            now += info.stability.duration
            info = Fsrs.review(
                info = info,
                rating = rating,
                now = now,
            )
            reviews += Review(
                timeMinutes = (now - start).inWholeMilliseconds / 60_000.0,
                stabilityMinutes = info.stability.minutes,
                difficulty = info.difficulty.value,
            )
        }
        return reviews
    }

    private fun List<Review>.reviewsWithin(
        minutes: Double,
    ): Int = count { review -> review.timeMinutes <= minutes }

    private fun assertClose(
        expected: Double,
        actual: Double,
        tolerance: Double = 1e-2,
    ) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun `good first day follows pure fsrs`() {
        val reviews = simulate(
            firstRating = FsrsRating.Good,
            nextRatings = List(4) { FsrsRating.Good },
        )

        assertEquals(4, reviews.reviewsWithin(24.0 * 60.0))
        assertClose(2.3065, reviews[0].stabilityMinutes)
        assertClose(34.56, reviews[1].stabilityMinutes, tolerance = 1e-1)
        assertClose(342.41, reviews[2].stabilityMinutes, tolerance = 1e-1)
        assertClose(2423.95, reviews[3].stabilityMinutes, tolerance = 1.0)
        assertTrue(reviews[3].timeMinutes <= 24.0 * 60.0)
        assertTrue(reviews[4].timeMinutes > 24.0 * 60.0)
    }

    @Test
    fun `again three times hard three times then good`() {
        val reviews = simulate(
            firstRating = FsrsRating.Again,
            nextRatings = List(2) { FsrsRating.Again } +
                List(3) { FsrsRating.Hard } +
                List(9) { FsrsRating.Good },
        )

        for (index in 0..2) {
            assertClose(1.0, reviews[index].stabilityMinutes)
        }
        assertClose(9.60, reviews[2].difficulty)
        assertClose(10.906, reviews[5].stabilityMinutes, tolerance = 1e-2)
        assertClose(9.86, reviews[5].difficulty)
        assertClose(25.960, reviews[6].stabilityMinutes, tolerance = 1e-2)

        assertEquals(12, reviews.reviewsWithin(24.0 * 60.0))
        assertTrue(reviews[11].timeMinutes <= 24.0 * 60.0)
        assertTrue(reviews[12].timeMinutes > 24.0 * 60.0)
    }

    @Test
    fun `again increases difficulty more than hard`() {
        val info = WordInfo(
            stability = Stability(2.days),
            difficulty = Difficulty(5.0),
            lastAnswerTimestamp = start,
        )
        val now = start + 2.days

        val again = Fsrs.review(
            info = info,
            rating = FsrsRating.Again,
            now = now,
        )
        val hard = Fsrs.review(
            info = info,
            rating = FsrsRating.Hard,
            now = now,
        )

        assertClose(8.3475, again.difficulty.value, tolerance = 1e-3)
        assertClose(6.6718, hard.difficulty.value, tolerance = 1e-3)
        assertTrue(again.difficulty.value > hard.difficulty.value)
    }

    @Test
    fun `success after a lapse grows stability`() {
        val reviews = simulate(
            firstRating = FsrsRating.Again,
            nextRatings = listOf(FsrsRating.Good),
        )

        assertTrue(reviews[1].stabilityMinutes > reviews[0].stabilityMinutes)
    }
}
