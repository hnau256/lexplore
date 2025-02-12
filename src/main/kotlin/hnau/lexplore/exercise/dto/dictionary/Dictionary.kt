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
data class Dictionary(
    val name: String,
    val words: List<Word>,
) {

    companion object {

        private val providers: List<DictionariesProvider> = listOf(
            SimpleDictionariesProvider,
            VerbsDictionariesProvider,
        )

        suspend fun loadList(
            context: Context,
        ): List<Dictionary> = coroutineScope {
            providers
                .map { provider ->
                    async {
                        provider.loadList(
                            context = context,
                        )
                    }
                }
                .flatMap { deferredDictionaries ->
                    deferredDictionaries.await()
                }
                .sortedBy(Dictionary::name)
        }
    }
}