package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import hnau.lexplore.R
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

object DictionariesProvider {

    suspend fun loadList(
        context: Context,
    ): Dictionaries {
        val dictionariesJson: List<DictionaryJson> = withContext(Dispatchers.IO) {
            @OptIn(ExperimentalSerializationApi::class)
            Json.decodeFromStream(
                stream = context.resources.openRawResource(R.raw.dictionaries),
                deserializer = ListSerializer(DictionaryJson.serializer()),
            )
        }
        return withContext(Dispatchers.Default) {
            dictionariesJson
                .associate { dictionaryJson ->
                    val name = DictionaryName(dictionaryJson.name)
                    val dictionary = Dictionary.create(
                        words = dictionaryJson.words.map { word ->
                            Word(
                                weight = word.weight,
                                toLearn = WordToLearn(word.word),
                                translation = Translation(word.translation),
                            )
                        }
                    )
                    name to dictionary
                }
                .let(::Dictionaries)
        }
    }

    private const val dictionariesPath = "data/dictionaries/"
}