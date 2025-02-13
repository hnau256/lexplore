package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.Dictionary
import hnau.lexplore.exercise.dto.dictionary.DictionaryName

interface DictionariesProvider {

    suspend fun loadList(
        context: Context,
    ): Dictionaries
}