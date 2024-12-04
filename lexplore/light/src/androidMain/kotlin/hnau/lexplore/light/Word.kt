package hnau.lexplore.light

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@Serializable
data class Word(
    val word: String,
    val count: Int,
) {

    companion object {

        fun loadList(
            context: Context,
        ): List<Word> = context
            .assets
            .open("dictionary.json")
            .let { dictionaryStream ->
                Json.decodeFromStream(
                    ListSerializer(serializer()),
                    dictionaryStream
                )
            }
    }
}