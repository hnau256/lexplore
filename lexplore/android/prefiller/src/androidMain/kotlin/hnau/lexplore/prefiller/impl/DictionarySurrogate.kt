package hnau.lexplore.prefiller.impl

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.toEnum
import hnau.common.kotlin.serialization.MappingKSerializer
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.data.api.dictionary.dto.Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
data class DictionarySurrogate(

    @SerialName("id")
    @Serializable(IdSerializer::class)
    val id: DictionaryInfo.Id,

    @SerialName("name")
    val name: String,

    @SerialName("main_language")
    @Serializable(LanguageSerializer::class)
    val mainLanguage: Language,

    @SerialName("columns")
    @Serializable(NonEmptyListSerializer::class)
    val columns: NonEmptyList<Column>,

    @SerialName("values")
    @Serializable(NonEmptyListSerializer::class)
    val values: NonEmptyList<Map<@Serializable(ColumnIdSerializer::class) DictionaryInfo.Column.Id, String>>,
) {

    data object IdSerializer : MappingKSerializer<String, DictionaryInfo.Id>(
        base = String.serializer(),
        mapper = Mapper(DictionaryInfo::Id, DictionaryInfo.Id::id),
    )

    data object ColumnIdSerializer : MappingKSerializer<String, DictionaryInfo.Column.Id>(
        base = String.serializer(),
        mapper = Mapper(DictionaryInfo.Column::Id, DictionaryInfo.Column.Id::id),
    )

    data object LanguageSerializer : MappingKSerializer<String, Language>(
        base = String.serializer(),
        mapper = Language.codeMapper,
    )

    @Serializable
    data class Column(

        @SerialName("id")
        @Serializable(ColumnIdSerializer::class)
        val id: DictionaryInfo.Column.Id,

        @SerialName("name")
        val name: String,

        @SerialName("custom_language")
        @Serializable(LanguageSerializer::class)
        val customLanguage: Language? = null,
    )

    fun toDictionaryValidated(): Pair<DictionaryInfo.Id, Dictionary>? {
        val dictionary = Dictionary.createValidated(
            info = DictionaryInfo(
                name = name,
                mainLanguage = mainLanguage,
                columns = columns.map { column ->
                    DictionaryInfo.Column(
                        id = column.id,
                        name = column.name,
                        customLanguage = column.customLanguage,
                    )
                },
            ),
            values = values,
        ) ?: return null
        return id to dictionary
    }

    companion object {

        fun fromDictionary(
            id: DictionaryInfo.Id,
            dictionary: Dictionary,
        ): DictionarySurrogate = DictionarySurrogate(
            id = id,
            name = dictionary.info.name,
            mainLanguage = dictionary.info.mainLanguage,
            values = dictionary.values,
            columns = dictionary.info.columns.map { column ->
                Column(
                    id = column.id,
                    name = column.name,
                    customLanguage = column.customLanguage,
                )
            }
        )
    }
}