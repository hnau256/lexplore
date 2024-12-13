package hnau.lexplore.model.dictionaries.impl

import hnau.common.kotlin.coroutines.mapState
import hnau.lexplore.data.api.dictionary.dto.DictionaryInfo
import hnau.lexplore.model.dictionaries.api.DictionariesModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal class DictionariesModelImpl(
    scope: CoroutineScope,
    private val skeleton: DictionariesModel.Skeleton,
    dependencies: DictionariesModel.Dependencies,
) : DictionariesModel {

    override val dictionaries: StateFlow<List<DictionariesModel.Dictionary>> = dependencies
        .dictionariesFlow
        .dictionaries
        .mapState(
            scope = scope,
        ) { dictionariesMap ->
            dictionariesMap.map { (id, info) ->
                DictionariesModel.Dictionary(
                    id = id,
                    info = info,
                )
            }
        }
}
