package hnau.lexplore.common

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryWord(
    val weight: WordWeight,
    val toLearn: WordToLearn,
    val translation: Translation,
)
