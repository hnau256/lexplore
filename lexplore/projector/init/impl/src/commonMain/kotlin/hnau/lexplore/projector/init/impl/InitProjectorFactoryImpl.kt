package hnau.lexplore.projector.init.impl

import hnau.lexplore.projector.init.api.InitProjector
import hnau.lexplore.projector.mainstack.api.MainStackProjector

fun InitProjector.Factory.Companion.impl(
    mainStackProjectorFactory: MainStackProjector.Factory,
): InitProjector.Factory = InitProjector.Factory { scope, dependencies, model ->
    InitProjectorImpl(
        scope = scope,
        dependencies = dependencies,
        model = model,
        mainStackProjectorFactory = mainStackProjectorFactory,
    )
}