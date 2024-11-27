package hnau.common.kotlin

data class Tagged<T, TAG>(
    val value: T,
    val tag: TAG,
)

inline fun <I, O, TAG> Tagged<I, TAG>.map(
    transform: (I) -> O,
): Tagged<O, TAG> = Tagged(
    value = transform(value),
    tag = tag,
)

fun <T, TAG> T.tagged(
    tag: TAG,
): Tagged<T, TAG> = Tagged(
    value = this,
    tag = tag,
)