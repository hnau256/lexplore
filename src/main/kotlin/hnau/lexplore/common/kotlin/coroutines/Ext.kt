package hnau.lexplore.common.kotlin.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

inline fun <T> Flow<T>.onEachLatest(
    crossinline action: suspend (T) -> Unit,
): Flow<T> = transformLatest { value ->
    action(value)
    emit(value)
}
