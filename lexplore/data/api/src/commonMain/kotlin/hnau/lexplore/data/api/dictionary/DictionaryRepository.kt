package hnau.lexplore.data.api.dictionary

import hnau.lexplore.data.api.dictionary.dto.Dictionary
import kotlinx.coroutines.flow.StateFlow

interface DictionaryRepository {

    suspend fun getDictionaries(): StateFlow<Map<Dictionary.Id, Dictionary>>
}