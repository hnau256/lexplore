package hnau.lexplore.model.common.ornull

sealed interface ValueOrCancel<out T> {

    data class Cancel(
        val cancel: () -> Unit,
    ) : ValueOrCancel<Nothing>

    data class Value<out T>(
        val value: T,
    ) : ValueOrCancel<T>
}


inline fun <I, O> ValueOrCancel<I>.map(
    transform: (I) -> O,
): ValueOrCancel<O> = when (this) {
    is ValueOrCancel.Cancel -> this

    is ValueOrCancel.Value ->
        ValueOrCancel.Value(transform(value))
}