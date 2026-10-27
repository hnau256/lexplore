package hnau.lexplore.fsrs.dto

import kotlin.jvm.JvmInline

@JvmInline
value class Difficulty(
    val value: Double,
) : Comparable<Difficulty> {

    operator fun plus(
        other: Difficulty,
    ): Difficulty = Difficulty(value + other.value)

    operator fun minus(
        other: Difficulty,
    ): Difficulty = Difficulty(value - other.value)

    operator fun times(
        factor: Double,
    ): Difficulty = Difficulty(value * factor)

    operator fun div(
        divisor: Double,
    ): Difficulty = Difficulty(value / divisor)

    override fun compareTo(
        other: Difficulty,
    ): Int = value.compareTo(other.value)

    companion object {

        val Min = Difficulty(1.0)

        val Max = Difficulty(10.0)

        val Range: ClosedRange<Difficulty> = Min..Max

        internal val Span: Double = Max.value - Min.value
    }
}
