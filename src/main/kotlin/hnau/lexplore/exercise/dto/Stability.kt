package hnau.lexplore.exercise.dto

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@JvmInline
value class Stability(
    val days: Double,
) : Comparable<Stability> {

    override fun compareTo(
        other: Stability,
    ): Int = days.compareTo(other.days)

    companion object {

        fun from(
            duration: Duration,
        ): Stability = Stability(duration / 1.days)
    }
}
