package hnau.lexplore.exercise.dto

@JvmInline
value class Retention(
    val value: Double,
) : Comparable<Retention> {

    override fun compareTo(
        other: Retention,
    ): Int = value.compareTo(other.value)
}
