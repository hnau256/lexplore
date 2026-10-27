package hnau.lexplore.common.dictionary.provider

import hnau.lexplore.common.DictionaryWord
import hnau.lexplore.common.Translation
import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.WordWeight
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.dictionary.Dictionary
import hnau.lexplore.common.dictionary.DictionaryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object DictionariesProvider {

    suspend fun load(
        json: String,
    ): Dictionaries {
        val dictionariesJson: List<DictionaryJson> = withContext(Dispatchers.Default) {
            Json.decodeFromString(
                deserializer = ListSerializer(DictionaryJson.serializer()),
                string = json,
            )
        }
        return withContext(Dispatchers.Default) {
            dictionariesJson
                .associate { dictionaryJson ->
                    val name = DictionaryName(dictionaryJson.name)
                    val dictionary = Dictionary.create(
                        dictionaryWords = dictionaryJson.words.map { word ->
                            DictionaryWord(
                                weight = WordWeight(word.weight),
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
}
