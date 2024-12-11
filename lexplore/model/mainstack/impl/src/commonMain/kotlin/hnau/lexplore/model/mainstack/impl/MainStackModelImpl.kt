package hnau.lexplore.model.mainstack.impl

import hnau.lexplore.model.mainstack.api.MainStackModel
import kotlinx.coroutines.CoroutineScope

internal class MainStackModelImpl(
    scope: CoroutineScope,
    private val skeleton: MainStackModel.Skeleton,
    private val dependencies: MainStackModel.Dependencies,
) : MainStackModel
