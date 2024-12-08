package hnau.lexplore.data.api.dictionary.dto

import arrow.core.NonEmptyList

data class Dictionary(
    val name: String,
    val mainLanguage: Language,
    val columns: NonEmptyList<Column>,
    val rows: NonEmptyList<(Column.Id) -> String>
) {

    @JvmInline
    value class Id(
        val id: String,
    )

    data class Column(
        val id: Id,
        val name: String,
        val customLanguage: Language? = null,
    ) {

        @JvmInline
        value class Id(
            val id: String,
        )
    }
}