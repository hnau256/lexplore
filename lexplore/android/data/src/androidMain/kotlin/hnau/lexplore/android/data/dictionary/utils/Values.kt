package hnau.lexplore.android.data.dictionary.utils

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

@Serializable
internal data class Values(
    val values: List<Map<String, String>>,
) {

    companion object {

        val serializer: KSerializer<Values> = MappingKSerializer(
            base = ListSerializer(MapSerializer(String.serializer(), String.serializer())),
            mapper = Mapper(::Values, Values::values),
        )

        val empty: Values = Values(
            values = emptyList(),
        )
    }

}