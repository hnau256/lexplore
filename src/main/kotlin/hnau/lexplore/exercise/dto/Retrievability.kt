package hnau.lexplore.exercise.dto

@JvmInline
value class Retrievability(
    val value: Double,
) : Comparable<Retrievability> {

    override fun compareTo(
        other: Retrievability,
    ): Int = value.compareTo(other.value)
}
