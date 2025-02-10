package hnau.lexplore.common.kotlin.mapper

import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.joinEscaped
import hnau.lexplore.common.kotlin.splitEscaped

fun Mapper.Companion.stringToStringsPairBySeparator(
    separator: Char,
    escape: Char = '\\',
) = Mapper<String, Pair<String, String>>(
    direct = { string ->
        string
            .splitEscaped(separator, escape)
            .takeIf { parts -> parts.size == 2 }
            .ifNull { throw IllegalArgumentException("Unable split $string to 2 parts by separator $separator") }
            .let { (first, second) ->
                first to second
            }
    },
    reverse = { keyWithValue ->
        keyWithValue.toList().joinEscaped(separator, escape)
    },
)
