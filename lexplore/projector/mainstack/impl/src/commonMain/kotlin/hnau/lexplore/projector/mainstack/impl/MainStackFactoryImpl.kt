package hnau.lexplore.projector.mainstack.impl

import hnau.lexplore.projector.dictionaries.api.DictionariesProjector
import hnau.lexplore.projector.mainstack.api.MainStackProjector

fun MainStackProjector.Factory.Companion.impl(
    dictionariesProjectorFactory: DictionariesProjector.Factory,
): MainStackProjector.Factory = MainStackProjector.Factory { scope, dependencies, model ->
    MainStackProjectorImpl(
        scope = scope,
        dependencies = dependencies,
        model = model,
        dictionariesProjectorFactory = dictionariesProjectorFactory,
    )
}