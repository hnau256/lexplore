package hnau.lexplore.model.init.impl

import hnau.lexplore.model.init.api.InitModel

fun InitModel.Factory.Companion.impl(): InitModel.Factory = InitModel.Factory { scope, skeleton, dependencies ->
    InitModelImpl(
        scope = scope,
        skeleton = skeleton,
        dependencies = dependencies,
    )
}