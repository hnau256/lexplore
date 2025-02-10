package hnau.lexplore.common.model.goback

import hnau.lexplore.common.kotlin.coroutines.flatMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

val NeverGoBackHandler: GoBackHandler = MutableStateFlow(null)

fun GoBackHandler.fallback(
    scope: CoroutineScope,
    fallback: GoBackHandler,
): GoBackHandler = flatMapState(scope) { goBackOrNull ->
    goBackOrNull
        ?.let(::MutableStateFlow)
        ?: fallback
}
