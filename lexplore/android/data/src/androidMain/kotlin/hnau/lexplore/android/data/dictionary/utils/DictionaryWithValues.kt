package hnau.lexplore.android.data.dictionary.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import arrow.core.NonEmptyList
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.data.api.dictionary.dto.Language

@Entity(
    tableName = DictionaryWithValues.table,
)
internal data class DictionaryWithValues(

    @PrimaryKey
    @ColumnInfo(
        name = columnId,
    )
    val id: String,

    @ColumnInfo(
        name = columnName,
    )
    val name: String,

    @ColumnInfo(
        name = columnMainLanguage,
    )
    val mainLanguage: Language,

    @ColumnInfo(
        name = columnColumns,
    )
    val columns: NonEmptyList<DictionaryInfo.Column>,

    @ColumnInfo(
        name = columnValues,
    )
    val values: Values,
) {

    companion object {

        const val table = "dictionary"
        const val columnId = "id"
        const val columnName = "name"
        const val columnMainLanguage = "main_language"
        const val columnColumns = "columns"
        const val columnValues = "values"
    }
}