package hnau.lexplore.model.dictionaries.impl

import hnau.lexplore.model.dictionaries.api.DictionariesModel
import kotlinx.coroutines.CoroutineScope

internal class DictionariesModelImpl(
    scope: CoroutineScope,
    private val skeleton: DictionariesModel.Skeleton,
    private val dependencies: DictionariesModel.Dependencies,
) : DictionariesModel
