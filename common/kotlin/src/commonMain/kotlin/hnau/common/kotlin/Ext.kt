package hnau.common.kotlin

inline fun <T> T?.ifNull(
    ifNull: () -> T,
) = this ?: ifNull()

inline fun <reified O> Any?.castOrElse(
    elseAction: () -> O,
) = when (this) {
    is O -> this
    else -> elseAction()
}

inline fun <reified O> Any?.castOrNull() = this as? O

inline fun <reified O> Any?.castOrThrow() = this as O

@Suppress("NOTHING_TO_INLINE")
inline fun <T> it(it: T) = it

inline fun <reified T> T.remindType(): T = this

inline fun <reified T, S> T.remindSupertype(): T where T : S = this
