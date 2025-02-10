package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import android.content.res.AssetManager
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SimpleDictionariesProvider : DictionariesProvider {

    override suspend fun loadList(
        context: Context,
    ): List<Dictionary> {
        val assets = context.assets
        val names: Array<out String> = withContext(Dispatchers.IO) {
            assets
                .list(dictionariesPath)
                .orEmpty()
        }
        return names.map { dictionaryFileName ->
            val name = dictionaryFileName.removeSuffix(".txt")
            if (name == dictionaryFileName) {
                error("Expected '*.txt' file, got $dictionaryFileName")
            }
            Dictionary(
                name = name,
                loadWords = {
                    loadWords(
                        assets = assets,
                        dictionaryFileName = dictionaryFileName,
                    )
                }
            )
        }
    }

    private suspend fun loadWords(
        assets: AssetManager,
        dictionaryFileName: String,
    ): List<Word> = DictionariesProviderUtils
        .readBlocksWithIndexRanges(
            stream = assets.open(dictionariesPath + dictionaryFileName)
        )
        .flatMap {indexedWords ->
            indexedWords
                .build()
        }
        .map { (weight, wordWithTranslation) ->
            val (word, translation) = wordWithTranslation
                .split('|')
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .takeIf { it.size == 2 }
                .ifNull { throw IllegalArgumentException("Expected '<greekWord>|<translation>', got $wordWithTranslation") }
            Word(
                index = weight,
                toLearn = WordToLearn(word),
                translation = Translation(translation)
            )
        }
        .toList()

    private const val dictionariesPath = "dictionaries/"
}