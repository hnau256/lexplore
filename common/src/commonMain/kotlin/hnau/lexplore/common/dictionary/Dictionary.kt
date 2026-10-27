package hnau.lexplore.common.dictionary

import hnau.lexplore.common.DictionaryWord
import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Dictionary private constructor(
    val dictionaryWords: List<DictionaryWord>,
) {

    companion object {

        fun create(
            dictionaryWords: List<DictionaryWord>,
        ): Dictionary = Dictionary(
            dictionaryWords = dictionaryWords.sortedByDescending(DictionaryWord::weight)
        )
    }
}
