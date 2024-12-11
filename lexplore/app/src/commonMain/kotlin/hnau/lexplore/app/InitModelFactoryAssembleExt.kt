package hnau.lexplore.app

import hnau.lexplore.model.dictionaries.api.DictionariesModel
import hnau.lexplore.model.dictionaries.impl.impl
import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.init.impl.impl
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.model.mainstack.impl.impl

internal fun InitModel.Factory.Companion.assemble(): InitModel.Factory {
    val dictionaries = DictionariesModel.Factory.impl()
    val mainStack = MainStackModel.Factory.impl(
        dictionariesModelFactory = dictionaries,
    )
    val init = InitModel.Factory.impl(
        mainStackModelFactory = mainStack,
    )
    return init
}