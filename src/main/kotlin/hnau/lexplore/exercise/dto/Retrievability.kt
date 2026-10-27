package hnau.lexplore.exercise.dto

@JvmInline
value class Retrievability(
    val value: Double,
) : Comparable<Retrievability> {

    val complement: Retrievability
        get() = Retrievability(1.0 - value)

    operator fun plus(
        other: Retrievability,
    ): Retrievability = Retrievability(value + other.value)

    operator fun minus(
        other: Retrievability,
    ): Retrievability = Retrievability(value - other.value)

    operator fun times(
        factor: Double,
    ): Retrievability = Retrievability(value * factor)

    operator fun div(
        divisor: Double,
    ): Retrievability = Retrievability(value / divisor)

    override fun compareTo(
        other: Retrievability,
    ): Int = value.compareTo(other.value)
}
