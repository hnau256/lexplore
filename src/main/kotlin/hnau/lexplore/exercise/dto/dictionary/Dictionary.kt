package hnau.lexplore.exercise.dto.dictionary

import android.content.Context
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.dictionary.provider.DictionariesProvider
import hnau.lexplore.exercise.dto.dictionary.provider.SimpleDictionariesProvider
import hnau.lexplore.exercise.dto.dictionary.provider.VerbsDictionariesProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

@Serializable
data class Dictionary private constructor(
    val words: List<Word>,
) {

    companion object {

        fun create(
            words: List<Word>,
        ): Dictionary = Dictionary(
            words = words.sortedBy { it.index }
        )

        private val providers: List<DictionariesProvider> = listOf(
            SimpleDictionariesProvider,
            VerbsDictionariesProvider,
        )

        suspend fun loadList(
            context: Context,
        ): Dictionaries = coroutineScope {
            providers
                .map { provider ->
                    async {
                        provider.loadList(
                            context = context,
                        )
                    }
                }
                .fold(
                    initial = Dictionaries.empty,
                ) { acc, dictionaries ->
                    acc + dictionaries.await()
                }
        }
    }
}