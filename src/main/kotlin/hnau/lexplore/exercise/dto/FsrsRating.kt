package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
enum class FsrsRating(
    val grade: Int,
) {
    Again(1),
    Hard(2),
    Easy(4),
    Good(3),
}
