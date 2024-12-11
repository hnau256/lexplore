package hnau.lexplore.prefiller.api

import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo

interface PrefillDataProvider {

    suspend fun getPrefillDictionaries(): Map<DictionaryInfo.Id, Dictionary>
}