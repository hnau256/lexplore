package hnau.lexplore.fsrs.dto

import kotlin.jvm.JvmInline

@JvmInline
value class Retention(
    val value: Double,
) : Comparable<Retention> {

    override fun compareTo(
        other: Retention,
    ): Int = value.compareTo(other.value)
}
