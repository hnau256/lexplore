package hnau.lexplore.compose

import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.init.impl.impl

internal fun InitProjector.Factory.Companion.assemble(): InitProjector.Factory {
    val init = InitProjector.Factory.impl()
    return init
}