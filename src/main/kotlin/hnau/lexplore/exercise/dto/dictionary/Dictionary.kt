package hnau.lexplore.exercise.dto.dictionary

import android.content.Context
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.dictionary.provider.DictionariesProvider
import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Dictionary private constructor(
    val words: List<Word>,
) {

    companion object {

        fun create(
            words: List<Word>,
        ): Dictionary = Dictionary(
            words = words.sortedByDescending(Word::weight)
        )

        suspend fun loadList(
            context: Context,
        ): Dictionaries = DictionariesProvider.loadList(
            context = context,
        )
    }
}