package hnau.lexplore.prefiller.impl

import android.content.Context
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.prefiller.api.PrefillDataProvider

class AndroidPrefillDataProvider(
    private val context: Context,
) : PrefillDataProvider {

    override suspend fun getPrefillDictionaries(): Map<DictionaryInfo.Id, Dictionary> {
        TODO("Not yet implemented")
    }
}