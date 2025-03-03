package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryWord(
    val weight: WordWeight,
    val toLearn: WordToLearn,
    val translation: Translation,
)