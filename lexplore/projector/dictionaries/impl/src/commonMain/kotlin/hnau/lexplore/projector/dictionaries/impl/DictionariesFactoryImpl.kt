package hnau.lexplore.projector.dictionaries.impl

import hnau.lexplore.projector.dictionaries.api.DictionariesProjector

fun DictionariesProjector.Factory.Companion.impl(): DictionariesProjector.Factory = DictionariesProjector.Factory { scope, dependencies, model ->
    DictionariesProjectorImpl(
        scope = scope,
        dependencies = dependencies,
        model = model,
    )
}