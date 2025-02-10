package hnau.lexplore.common.model.storage

import hnau.lexplore.common.kotlin.coroutines.combineState
import hnau.lexplore.common.kotlin.coroutines.operationOrNullIfExecuting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

data class InstantStorageEntry<T>(
    val value: T,
    val updateValue: ((T) -> Unit)?,
)


fun <T> StorageEntry<T>.toInstant(
    scope: CoroutineScope,
): StateFlow<InstantStorageEntry<T>> = combineState(
    scope = scope,
    a = value,
    b = operationOrNullIfExecuting(
        scope = scope,
        action = ::updateValue,
    ),
    combine = ::InstantStorageEntry,
)
