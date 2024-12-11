package hnau.lexplore.model.init.impl

import hnau.lexplore.model.init.api.InitModel
import hnau.lexplore.model.mainstack.api.MainStackModel
import kotlinx.coroutines.CoroutineScope

internal class InitModelImpl(
    scope: CoroutineScope,
    private val skeleton: InitModel.Skeleton,
    private val dependencies: InitModel.Dependencies,
    private val mainStackModelFactory: MainStackModel.Factory,
) : InitModel
