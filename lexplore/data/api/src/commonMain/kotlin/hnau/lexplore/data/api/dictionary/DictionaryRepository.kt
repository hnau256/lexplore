package hnau.lexplore.data.api.dictionary

import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import kotlinx.coroutines.flow.StateFlow

interface DictionaryRepository {

    suspend fun getDictionaries(): StateFlow<Map<DictionaryInfo.Id, DictionaryInfo>>
}