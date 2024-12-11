package hnau.lexplore.projector.mainstack.impl

import hnau.lexplore.projector.mainstack.api.MainStackProjector

fun MainStackProjector.Factory.Companion.impl(): MainStackProjector.Factory = MainStackProjector.Factory { scope, dependencies, model ->
    MainStackProjectorImpl(
        scope = scope,
        dependencies = dependencies,
        model = model,
    )
}