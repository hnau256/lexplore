package hnau.lexplore.data.api.dictionary.dto

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

class Dictionary private constructor(
    val info: DictionaryInfo,
    val values: NonEmptyList<Map<DictionaryInfo.Column.Id, String>>,
) {

    companion object {

        fun createValidated(
            info: DictionaryInfo,
            values: NonEmptyList<Map<DictionaryInfo.Column.Id, String>>,
        ): Dictionary? {
            val expectedColumns: Set<DictionaryInfo.Column.Id> = info
                .columns
                .map(DictionaryInfo.Column::id)
                .toSet()
            values.forEach { row ->
                if (row.keys != expectedColumns) {
                    return null
                }
            }
            return Dictionary(
                info = info,
                values = values,
            )
        }
    }
}