package hnau.common.kotlin

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Timestamped<T>(
    val timestamp: Instant,
    val value: T,
) {

    inline fun <R> map(
        transform: (T) -> R,
    ): Timestamped<R> = Timestamped(
        timestamp = timestamp,
        value = transform(value),
    )
}