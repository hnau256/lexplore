package hnau.lexplore.data.api.dictionary.dto

data class Dictionary(
    val info: DictionaryInfo,
    val values: List<Map<DictionaryInfo.Column.Id, String>>,
)