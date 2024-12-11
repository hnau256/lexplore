package hnau.lexplore.data.api.dictionary.dto

import kotlinx.coroutines.flow.StateFlow

data class DictionariesFlow(
    val dictionaries: StateFlow<Map<DictionaryInfo.Id, DictionaryInfo>>,
)