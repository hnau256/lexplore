package hnau.lexplore.exercise.dto

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@JvmInline
value class Stability(
    val duration: Duration,
) : Comparable<Stability> {

    internal val days: Double
        get() = duration / 1.days

    val inWholeDays: Long
        get() = duration.inWholeDays

    operator fun plus(
        other: Stability,
    ): Stability = Stability(duration + other.duration)

    operator fun plus(
        other: Duration,
    ): Stability = Stability(duration + other)

    operator fun minus(
        other: Stability,
    ): Stability = Stability(duration - other.duration)

    operator fun minus(
        other: Duration,
    ): Stability = Stability(duration - other)

    operator fun times(
        factor: Double,
    ): Stability = Stability(duration * factor)

    operator fun div(
        divisor: Double,
    ): Stability = Stability(duration / divisor)

    operator fun div(
        other: Stability,
    ): Double = duration / other.duration

    override fun compareTo(
        other: Stability,
    ): Int = duration.compareTo(other.duration)
}
