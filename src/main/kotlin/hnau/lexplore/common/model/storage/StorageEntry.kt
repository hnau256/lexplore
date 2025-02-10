package hnau.lexplore.common.model.storage

import kotlinx.coroutines.flow.StateFlow

interface StorageEntry<T> {

    val value: StateFlow<T>

    suspend fun updateValue(newValue: T)
}
