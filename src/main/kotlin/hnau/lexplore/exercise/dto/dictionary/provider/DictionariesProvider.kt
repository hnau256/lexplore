package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DictionariesProvider {

    suspend fun loadList(
        context: Context,
    ): Dictionaries {
        val assets = context.assets
        val names: Array<out String> = withContext(Dispatchers.IO) {
            assets
                .list(dictionariesPath)
                .orEmpty()
        }
        val frequencies = Frequencies.create(
            context = context,
        )
        return Dictionaries(
            names.associate { dictionaryFileName ->
                val name = dictionaryFileName.removeSuffix(".txt")
                if (name == dictionaryFileName) {
                    error("Expected '*.txt' file, got $dictionaryFileName")
                }
                val dictionary = Dictionary.create(
                    words = loadWords(
                        assets = assets,
                        dictionaryFileName = dictionaryFileName,
                        frequencies = frequencies,
                    )
                )
                DictionaryName(name) to dictionary
            }
        )
    }

    private suspend fun loadWords(
        assets: AssetManager,
        dictionaryFileName: String,
        frequencies: Frequencies,
    ): List<Word> = DictionariesProviderUtils
        .readLines(
            stream = assets.open(dictionariesPath + dictionaryFileName)
        )
        .map { line ->
            val (word, translation) = line
                .split('|')
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .takeIf { it.size == 2 }
                .ifNull { throw IllegalArgumentException("Expected '<greekWord>|<translation>', got $line") }
            Word(
                weight = frequencies[word]?.toFloat().ifNull {
                    Log.d("QWERTY", "Unknown frequency of $word")
                    0f
                },
                toLearn = WordToLearn(word),
                translation = Translation(translation)
            )
        }
        .toList()

    private const val dictionariesPath = "data/dictionaries/"
}