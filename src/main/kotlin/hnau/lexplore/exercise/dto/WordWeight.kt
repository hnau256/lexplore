package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class WordWeight(
    val weight: Float,
) : Comparable<WordWeight> {

    override fun compareTo(
        other: WordWeight,
    ): Int = weight.compareTo(
        other.weight,
    )
}