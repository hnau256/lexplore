package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
data class WordToLearn(
    val word: String,
)