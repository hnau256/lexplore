package hnau.lexplore.android.data.dictionary

import android.content.Context
import hnau.common.kotlin.AsyncLazy
import hnau.common.kotlin.coroutines.toStateLite
import hnau.lexplore.data.api.dictionary.DictionaryRepository
import hnau.lexplore.data.api.dictionary.dto.DictionariesFlow
import hnau.lexplore.data.api.dictionary.dto.Dictionary
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.android.data.dictionary.utils.DictionaryDao
import hnau.lexplore.android.data.dictionary.utils.DictionaryWithValues
import hnau.lexplore.android.data.dictionary.utils.LexploreDatabase
import hnau.lexplore.android.data.dictionary.utils.Values
import kotlinx.coroutines.Dispatchers
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

    override suspend fun getDictionaries(): DictionariesFlow = DictionariesFlow(
        dictionaries = dictionaries.get(),
    )

    override suspend fun insertDictionary(
        id: DictionaryInfo.Id,
        dictionary: Dictionary,
    ) {
        withContext(Dispatchers.IO) {
            dictionariesDao
                .get()
                .insertOrUpdate(
                    DictionaryWithValues(
                        id = id.id,
                        name = dictionary.info.name,
                        columns = dictionary.info.columns,
                        mainLanguage = dictionary.info.mainLanguage,
                        values = Values(
                            dictionary.values.map { row ->
                                row.mapKeys { (columnId) -> columnId.id }
                            }
                        ),
                    )
                )
        }
    }
}