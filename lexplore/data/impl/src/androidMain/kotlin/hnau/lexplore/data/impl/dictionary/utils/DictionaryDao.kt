package hnau.lexplore.data.impl.dictionary.utils

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
internal interface DictionaryDao {

    @Query(DictionaryInfoViewQuerySQL)
    fun getAllDictionariesInfo(): Flow<List<DictionaryInfoView>>
}