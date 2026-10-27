package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.exercise.dto.Stability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class FsrsIntervalsTest {

    private val start = Instant.fromEpochMilliseconds(0)

    private data class Review(
        val timeMinutes: Double,
        val stabilityMinutes: Double,
    )

    private val Stability.duration: Duration
        get() = days.days

    private val Stability.minutes: Double
        get() = days * 24.0 * 60.0

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
        tolerance: Double = 1e-6,
    ) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun `ten good reviews fit into the first day`() {
        val reviews = simulate(
            firstRating = FsrsRating.Good,
            nextRatings = List(19) { FsrsRating.Good },
        )

        assertEquals(10, reviews.reviewsWithin(24.0 * 60.0))
        assertClose(2.3065, reviews[0].stabilityMinutes)
        for (index in 1..10) {
            assertClose(
                expected = 1.9,
                actual = reviews[index].stabilityMinutes / reviews[index - 1].stabilityMinutes,
            )
        }
        assertTrue(reviews[9].timeMinutes <= 24.0 * 60.0)
        assertTrue(reviews[10].timeMinutes > 24.0 * 60.0)
    }

    @Test
    fun `again three times hard three times then good`() {
        val reviews = simulate(
            firstRating = FsrsRating.Again,
            nextRatings = List(2) { FsrsRating.Again } +
                List(3) { FsrsRating.Hard } +
                List(19) { FsrsRating.Good },
        )

        for (index in 0..2) {
            assertClose(1.0, reviews[index].stabilityMinutes)
        }
        assertClose(5.0002, reviews[5].stabilityMinutes, tolerance = 1e-3)
        assertClose(9.5004, reviews[6].stabilityMinutes, tolerance = 1e-3)
        for (index in 7..14) {
            assertClose(
                expected = 1.9,
                actual = reviews[index].stabilityMinutes / reviews[index - 1].stabilityMinutes,
            )
        }

        assertEquals(14, reviews.reviewsWithin(24.0 * 60.0))
        assertTrue(reviews[13].timeMinutes <= 24.0 * 60.0)
        assertTrue(reviews[14].timeMinutes > 24.0 * 60.0)
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
