package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val index: Float,
    val toLearn: WordToLearn,
    val translation: Translation,
)