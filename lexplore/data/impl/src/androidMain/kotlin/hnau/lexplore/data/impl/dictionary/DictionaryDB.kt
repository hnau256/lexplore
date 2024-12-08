package hnau.lexplore.data.impl.dictionary

import androidx.room.ColumnInfo
import androidx.room.Entity
import arrow.core.NonEmptyList
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Entity(
    tableName = DictionaryDB.table,
)
data class DictionaryDB(

    @ColumnInfo(
        name = columnId,
    )
    val id: Dictionary.Id,

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
    val columns: NonEmptyList<Column>,
) {

    @Serializable
    data class Column(
        @Serializable(IdSerializer::class)
        @SerialName("id")
        val id: Dictionary.Column.Id,

        @SerialName("name")
        val name: String,

        @SerialName("custom_language")
        val customLanguage: Language? = null,
    ) {

        data object IdSerializer : MappingKSerializer<String, Dictionary.Column.Id>(
            base = String.serializer(),
            mapper = Mapper(Dictionary.Column::Id, Dictionary.Column.Id::id),
        )
    }

    companion object {

        const val table = "dictionary"
        const val columnId = "id"
        const val columnName = "name"
        const val columnMainLanguage = "main_language"
        const val columnColumns = "columns"
        const val columnValues = "values"
    }
}