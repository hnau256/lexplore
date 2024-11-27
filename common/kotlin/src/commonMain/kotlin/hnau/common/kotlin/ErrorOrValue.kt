package hnau.common.kotlin

import arrow.core.Either

typealias ErrorOrValue<T> = Either<Throwable, T>