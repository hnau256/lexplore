package hnau.common.kotlin

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AsyncLazy<T>(
    private val get: suspend () -> T,
) {

    private var cachedValue: Option<T> = None

    private val getValueMutex: Mutex = Mutex()

    suspend fun get(): T = getValueMutex.withLock {
        cachedValue.getOrElse {
            get
                .invoke()
                .also { newValue ->
                    cachedValue = Some(newValue)
                }
        }
    }
}