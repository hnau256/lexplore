package hnau.lexplore.compose

import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import hnau.lexplore.projector.dictionaries.impl.impl
import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.init.impl.impl
import hnau.lexplore.projector.mainstack.api.MainStackProjector
import hnau.lexplore.projector.mainstack.impl.impl

internal fun InitProjector.Factory.Companion.assemble(): InitProjector.Factory {
    val dictionaries = DictionariesProjector.Factory.impl()
    val mainStack = MainStackProjector.Factory.impl(
        dictionariesProjectorFactory = dictionaries,
    )
    val init = InitProjector.Factory.impl(
        mainStackProjectorFactory = mainStack,
    )
    return init
}