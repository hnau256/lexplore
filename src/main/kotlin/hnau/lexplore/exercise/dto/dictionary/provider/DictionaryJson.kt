package hnau.lexplore.exercise.dto.dictionary.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryJson(

    @SerialName("name")
    val name: String,

    @SerialName("words")
    val words: List<Word>,
) {

    @Serializable
    data class Word(

        @SerialName("word")
        val word: String,

        @SerialName("translation")
        val translation: String,

        @SerialName("weight")
        val weight: Float,
    )
}