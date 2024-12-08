package hnau.lexplore.api.dictionary

import hnau.lexplore.api.dictionary.dto.Dictionary
import kotlinx.coroutines.flow.StateFlow

interface DictionaryRepository {

    suspend fun getDictionaries(): StateFlow<Map<Dictionary.Id, Dictionary>>
}