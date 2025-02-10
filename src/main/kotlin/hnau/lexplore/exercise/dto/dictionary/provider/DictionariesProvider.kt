package hnau.lexplore.exercise.dto.dictionary.provider

import android.content.Context
import hnau.lexplore.exercise.dto.dictionary.Dictionary

interface DictionariesProvider {

    suspend fun loadList(
        context: Context,
    ): List<Dictionary>
}