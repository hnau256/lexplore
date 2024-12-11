package hnau.lexplore.compose

import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.init.impl.impl
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import hnau.lexplore.projector.mainstack.impl.impl

internal fun InitProjector.Factory.Companion.assemble(): InitProjector.Factory {
    val mainStack = MainStackProjector.Factory.impl()
    val init = InitProjector.Factory.impl(
        mainStackProjectorFactory = mainStack,
    )
    return init
}