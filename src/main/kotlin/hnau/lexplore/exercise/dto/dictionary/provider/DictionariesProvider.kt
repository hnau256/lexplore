package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import hnau.lexplore.exercise.dto.dictionary.Dictionaries

interface DictionariesProvider {

    suspend fun loadList(
        context: Context,
    ): Dictionaries
}