package hnau.lexplore.android.data.dictionary.utils

import androidx.room.DatabaseView
import arrow.core.NonEmptyList
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.data.api.dictionary.dto.Language

internal const val DictionaryInfoViewQuerySQL = "SELECT " +
        "${DictionaryWithValues.columnId} as id, " +
        "${DictionaryWithValues.columnName} as name, " +
        "${DictionaryWithValues.columnMainLanguage} as mainLanguage, " +
        "${DictionaryWithValues.columnColumns} as columns " +
        "FROM ${DictionaryWithValues.table}"

@DatabaseView(DictionaryInfoViewQuerySQL)
internal data class DictionaryInfoView(
    val id: String,
    val name: String,
    val mainLanguage: Language,
    val columns: NonEmptyList<DictionaryInfo.Column>,
)