package hnau.lexplore.data.impl.dictionary.utils

import androidx.room.TypeConverter
import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.data.api.dictionary.dto.Language
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal data object DictionaryTypeConverters {

    @TypeConverter
    fun stringToColumns(
        string: String,
    ): NonEmptyList<DictionaryInfo.Column> = json.decodeFromString(
        deserializer = columnsSerializer,
        string = string,
    )

    @TypeConverter
    fun columnsToString(
        columns: NonEmptyList<DictionaryInfo.Column>,
    ): String = json.encodeToString(
        serializer = columnsSerializer,
        value = columns,
    )

    @TypeConverter
    fun stringToValues(
        string: String,
    ): Values = json.decodeFromString(
        deserializer = Values.serializer,
        string = string,
    )

    @TypeConverter
    fun valuesToString(
        values: Values,
    ): String = json.encodeToString(
        serializer = Values.serializer,
        value = values,
    )

    private val json: Json = Json {
        prettyPrint = false
    }

    private val columnsSerializer: NonEmptyListSerializer<DictionaryInfo.Column> =
        NonEmptyListSerializer(
            elementSerializer = MappingKSerializer(
                base = DictionaryInfoColumnSurrogate.serializer(),
                mapper = Mapper(
                    direct = { surrogate ->
                        DictionaryInfo.Column(
                            id = DictionaryInfo.Column.Id(surrogate.id),
                            name = surrogate.name,
                            customLanguage = surrogate.customLanguage,
                        )
                    },
                    reverse = { column ->
                        DictionaryInfoColumnSurrogate(
                            id = column.id.id,
                            name = column.name,
                            customLanguage = column.customLanguage,
                        )
                    }
                )
            ),
        )

    @Serializable
    private data class DictionaryInfoColumnSurrogate(
        @SerialName("id")
        val id: String,

        @SerialName("name")
        val name: String,

        @SerialName("custom_language")
        val customLanguage: Language? = null,
    )
}