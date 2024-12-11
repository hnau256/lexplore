package hnau.lexplore.model.mainstack.impl

import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.model.mainstack.api.MainStackModel

fun MainStackModel.Factory.Companion.impl(
    dictionariesModelFactory: DictionariesModel.Factory,
): MainStackModel.Factory = MainStackModel.Factory { scope, skeleton, dependencies ->
    MainStackModelImpl(
        scope = scope,
        skeleton = skeleton,
        dependencies = dependencies,
        dictionariesModelFactory = dictionariesModelFactory,
    )
}