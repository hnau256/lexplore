package hnau.lexplore.projector.init.impl

import hnau.lexplore.projector.init.api.InitProjector

fun InitProjector.Factory.Companion.impl(): InitProjector.Factory = InitProjector.Factory { scope, dependencies, model ->
    InitProjectorImpl(
        scope = scope,
        dependencies = dependencies,
        model = model,
    )
}