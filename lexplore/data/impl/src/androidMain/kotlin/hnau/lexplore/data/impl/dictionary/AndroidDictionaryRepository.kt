package hnau.lexplore.data.impl.dictionary

import android.content.Context
import hnau.common.kotlin.AsyncLazy
import hnau.common.kotlin.coroutines.toStateLite
import hnau.lexplore.data.api.dictionary.DictionaryRepository
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.data.impl.dictionary.utils.DictionaryDao
import hnau.lexplore.data.impl.dictionary.utils.LexploreDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AndroidDictionaryRepository(
    private val context: Context,
) : DictionaryRepository {

    private val dictionariesDao: AsyncLazy<DictionaryDao> = AsyncLazy {
        withContext(Dispatchers.IO) {
            LexploreDatabase
                .create(
                    context = context,
                )
                .dictionary
        }
    }

    private val dictionaries = AsyncLazy {
        withContext(Dispatchers.IO) {
            dictionariesDao
                .get()
                .getAllDictionariesInfo()
                .map { dictionariesList ->
                    dictionariesList.associate { info ->
                        val id = DictionaryInfo.Id(info.id)
                        val dictionaryInfo = DictionaryInfo(
                            name = info.name,
                            mainLanguage = info.mainLanguage,
                            columns = info.columns,
                        )
                        id to dictionaryInfo
                    }
                }
                .toStateLite()
        }
    }

    override suspend fun getDictionaries(): StateFlow<Map<DictionaryInfo.Id, DictionaryInfo>> =
        dictionaries.get()

}