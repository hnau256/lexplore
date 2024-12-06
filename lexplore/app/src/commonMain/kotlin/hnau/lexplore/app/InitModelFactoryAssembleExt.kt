package hnau.lexplore.app

import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.init.impl.impl

internal fun InitModel.Factory.Companion.assemble(): InitModel.Factory {
    val init = InitModel.Factory.impl()
    return init
}