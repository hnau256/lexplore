package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class KnowLevel(
    val level: Float,
) : Comparable<KnowLevel> {

    override fun compareTo(
        other: KnowLevel,
    ): Int = level.compareTo(
        other.level,
    )
}