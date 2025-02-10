package hnau.lexplore.common.model.stack

import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.model.goback.GoBackHandler
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun StateFlow<NonEmptyStack<GoBackHandlerProvider>>.tailGoBackHandler(
    scope: CoroutineScope,
): GoBackHandler = flatMapState(scope) { stack ->
    stack.tail.goBackHandler
}

fun <T> MutableStateFlow<NonEmptyStack<T>>.stackGoBackHandler(
    scope: CoroutineScope,
): GoBackHandler = mapState(scope) { stack ->
    val newStack = stack.tryDropLast() ?: return@mapState null
    return@mapState { value = newStack }
}
