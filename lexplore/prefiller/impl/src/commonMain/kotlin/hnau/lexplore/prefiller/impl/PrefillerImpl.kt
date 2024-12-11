package hnau.lexplore.prefiller.impl

import hnau.lexplore.data.api.dictionary.DictionaryRepository
import hnau.lexplore.prefiller.api.PrefillDataProvider
import hnau.lexplore.prefiller.api.Prefiller

class PrefillerImpl(
    private val prefillDataProvider: PrefillDataProvider,
) : Prefiller {

    override suspend fun prefillIfNeed(
        repository: DictionaryRepository,
    ) {
        prefillDictionariesIfNeed(
            repository = repository,
        )
    }

    private suspend fun prefillDictionariesIfNeed(
        repository: DictionaryRepository,
    ) {
        val existingDictionaries = repository.getDictionaries().dictionaries.value
        if (existingDictionaries.isNotEmpty()) {
            return
        }
        val prefillDictionaries = prefillDataProvider.getPrefillDictionaries()
        prefillDictionaries.forEach { (id, dictionary) ->
            repository.insertDictionary(
                id = id,
                dictionary = dictionary,
            )
        }
    }
}