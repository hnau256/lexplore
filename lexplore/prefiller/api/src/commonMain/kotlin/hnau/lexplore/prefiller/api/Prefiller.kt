package hnau.lexplore.prefiller.api

import hnau.lexplore.data.api.dictionary.DictionaryRepository

interface Prefiller {

    suspend fun prefillIfNeed(
        repository: DictionaryRepository,
    )
}