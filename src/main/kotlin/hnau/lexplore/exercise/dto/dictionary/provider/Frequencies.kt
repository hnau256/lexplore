package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import android.util.Log
import hnau.lexplore.common.kotlin.ifNull

class Frequencies private constructor(
    private val map: Map<String, Int>,
) {

    operator fun get(
        word: String,
    ): Int? {
        val preparedWord = word.trim().lowercase()
        return map[preparedWord]
    }

    companion object {

        suspend fun create(
            context: Context,
        ): Frequencies {
            val list =  DictionariesProviderUtils
                .readLines(
                    stream = context.assets.open("data/frequency.txt")
                )
                .map { line ->
                    val parts = line
                        .split('|')
                        .mapNotNull { it.trim().lowercase().takeIf(String::isNotEmpty) }
                        .takeIf { it.size == 14 }
                        .ifNull { throw IllegalArgumentException("Expected 'ID|Word|FREQcount|CD|SUBTLEX_WF|Lg10WF|SUBTLEX_CD|Lg10CD|FREQlow|FREQupper|N|OLD20|Length|SUBTLEX_WF_full', got $line") }
                    val word = parts[1].lowercase()
                    val frequency = parts[2].toInt()
                    word to frequency
                }
                .toList()
            val a = list
                .sortedByDescending { it.second }
                .take(50)
                .joinToString {(word, count) ->"$word:$count"

                }
            Log.d("QWERTY", a)
            val map = list.associate { it }
            Log.d("QWERTY", "Total:${list.size}, unique:${map.size}")
            return Frequencies(
                map = map,
            )
        }
    }
}