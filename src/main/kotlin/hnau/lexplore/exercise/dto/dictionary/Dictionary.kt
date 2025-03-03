package hnau.lexplore.exercise.dto.dictionary

import android.content.Context
import hnau.lexplore.exercise.dto.DictionaryWord
import hnau.lexplore.exercise.dto.dictionary.provider.DictionariesProvider
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

        suspend fun loadList(
            context: Context,
        ): Dictionaries = DictionariesProvider.loadList(
            context = context,
        )
    }
}