package hnau.lexplore.prefiller.impl

import android.content.Context
import android.content.res.AssetManager
import arrow.core.identity
import hnau.common.kotlin.ifNull
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.prefiller.api.PrefillDataProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

class AndroidPrefillDataProvider(
    private val context: Context,
) : PrefillDataProvider {

    override suspend fun getPrefillDictionaries(): Map<DictionaryInfo.Id, Dictionary> {
        return withContext(Dispatchers.IO) {
            val assetManager: AssetManager = context.assets
            val path = "prefill/dictionaries/"
            assetManager
                .list(path)
                .orEmpty()
                .map { fileName ->
                    async {
                        readDictionary(
                            fileName = path + fileName,
                            assetManager = assetManager,
                        )
                    }
                }
                .awaitAll()
                .filterNotNull()
                .associate(::identity)
        }
    }

    private suspend fun readDictionary(
        fileName: String,
        assetManager: AssetManager,
    ): Pair<DictionaryInfo.Id, Dictionary>? = try {
        logger.debug { "Reading prefill dictionary from file $fileName" }
        readDictionaryUnsafe(
            fileName = fileName,
            assetManager = assetManager,
        )
    } catch (th: Throwable) {
        logger.warn(th) { "Unable read prefill dictionary from file $fileName" }
        null
    }

    private suspend fun readDictionaryUnsafe(
        fileName: String,
        assetManager: AssetManager,
    ): Pair<DictionaryInfo.Id, Dictionary>? = assetManager
        .open(fileName)
        .reader()
        .readText()
        .let { dictionaryJson ->
            withContext(Dispatchers.Default) {
                json
                    .decodeFromString(
                        deserializer = DictionarySurrogate.serializer(),
                        string = dictionaryJson
                    )
                    .toDictionaryValidated()
                    .ifNull {
                        logger.warn { "Dictionary from file $fileName is broken" }
                        null
                    }
            }
        }

    companion object {

        private val json: Json = Json
    }
}