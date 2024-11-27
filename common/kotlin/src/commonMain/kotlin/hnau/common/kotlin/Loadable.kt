package hnau.common.kotlin

sealed class Loadable<out T> {

    data object Loading : Loadable<Nothing>()

    data class Ready<out T>(val value: T) : Loadable<T>()

    inline fun <R> fold(
        ifLoading: () -> R,
        ifReady: (T) -> R,
    ): R =
        when (this) {
            is Loading -> ifLoading()
            is Ready -> ifReady(value)
        }

    inline fun <R> map(
        transform: (T) -> R,
    ): Loadable<R> =
        fold(
            ifLoading = { Loading },
            ifReady = { Ready(transform(it)) },
        )

    inline fun <R> flatMap(
        transform: (T) -> Loadable<R>,
    ): Loadable<R> =
        fold(
            ifLoading = { Loading },
            ifReady = transform,
        )

    fun orNull(): T? = valueOrElse { null }
}

inline fun <T> Loadable<T>.valueOrElse(
    ifLoading: () -> T,
): T =
    fold(
        ifReady = { it },
        ifLoading = ifLoading,
    )
