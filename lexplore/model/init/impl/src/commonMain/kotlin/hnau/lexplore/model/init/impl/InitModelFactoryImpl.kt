package hnau.lexplore.model.init.impl

import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.mainstack.api.MainStackModel

fun InitModel.Factory.Companion.impl(
    mainStackModelFactory: MainStackModel.Factory,
): InitModel.Factory = InitModel.Factory { scope, skeleton, dependencies ->
    InitModelImpl(
        scope = scope,
        skeleton = skeleton,
        dependencies = dependencies,
        mainStackModelFactory = mainStackModelFactory,
    )
}