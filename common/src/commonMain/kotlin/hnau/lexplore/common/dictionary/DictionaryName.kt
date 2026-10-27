package hnau.lexplore.common.dictionary

import kotlin.jvm.JvmInline

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
