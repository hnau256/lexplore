package hnau.lexplore.exercise.dto.dictionary

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class DictionaryName(
    val name: String,
) : Comparable<DictionaryName> {

    override fun compareTo(
        other: DictionaryName,
    ): Int = name.compareTo(
        other = other.name,
    )
}