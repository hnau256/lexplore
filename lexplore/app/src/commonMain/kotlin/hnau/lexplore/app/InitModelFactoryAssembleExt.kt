package hnau.lexplore.app

import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.init.impl.impl
import hnau.lexplore.model.mainstack.api.MainStackModel
import hnau.lexplore.model.mainstack.impl.impl

internal fun InitModel.Factory.Companion.assemble(): InitModel.Factory {
    val mainStack = MainStackModel.Factory.impl()
    val init = InitModel.Factory.impl(
        mainStackModelFactory = mainStack,
    )
    return init
}