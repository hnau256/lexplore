package hnau.lexplore.model.dictionaries.impl

import hnau.lexplore.model.dictionaries.api.DictionariesModel

fun DictionariesModel.Factory.Companion.impl(): DictionariesModel.Factory = DictionariesModel.Factory { scope, skeleton, dependencies ->
    DictionariesModelImpl(
        scope = scope,
        skeleton = skeleton,
        dependencies = dependencies,
    )
}