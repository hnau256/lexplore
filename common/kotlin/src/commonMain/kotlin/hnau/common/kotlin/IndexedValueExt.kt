package hnau.common.kotlin

inline fun <I, O> IndexedValue<I>.map(
    transform: (I) -> O,
): IndexedValue<O> = IndexedValue(
    index = index,
    value = transform(value),
)
