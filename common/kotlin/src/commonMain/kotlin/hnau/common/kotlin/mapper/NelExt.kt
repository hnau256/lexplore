package hnau.common.kotlin.mapper

import arrow.core.Nel
import arrow.core.NonEmptyList

@Suppress("FunctionName")
fun <T> NelListMapper() = Mapper(
    direct = NonEmptyList.Companion::fromListUnsafe,
    reverse = Nel<T>::all,
)
