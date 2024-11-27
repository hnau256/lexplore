package hnau.common.kotlin

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

fun <L> Either<L, *>.leftOrNone(): Option<L> = fold(
    ifLeft = ::Some,
    ifRight = { None },
)