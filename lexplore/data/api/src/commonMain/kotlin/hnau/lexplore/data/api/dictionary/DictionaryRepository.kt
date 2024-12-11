package hnau.lexplore.data.api.dictionary

import hnau.lexplore.data.api.dictionary.dto.DictionariesFlow
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo

interface DictionaryRepository {

    suspend fun getDictionaries(): DictionariesFlow

    suspend fun insertDictionary(
        id: DictionaryInfo.Id,
        dictionary: Dictionary,
    )
}