package hnau.lexplore.common.model.goback

import hnau.lexplore.common.kotlin.coroutines.flatMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

fun StateFlow<GoBackHandlerProvider>.stateGoBackHandler(
    scope: CoroutineScope,
): GoBackHandler = flatMapState(scope) { it.goBackHandler }
