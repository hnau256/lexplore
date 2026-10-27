package hnau.lexplore.exercise.dto

@JvmInline
value class Difficulty(
    val value: Double,
) : Comparable<Difficulty> {

    override fun compareTo(
        other: Difficulty,
    ): Int = value.compareTo(other.value)
}
