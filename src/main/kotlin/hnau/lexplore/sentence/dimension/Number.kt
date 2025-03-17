package hnau.lexplore.sentence.dimension

import kotlinx.serialization.Serializable

@Serializable
enum class Number {
    Singular,
    Plural,
}

@Serializable
data class NumberValues<out T>(
    val singular: T,
    val plural: T,
) {

    operator fun get(
        number: Number,
    ): T = when (number) {
        Number.Singular -> singular
        Number.Plural -> plural
    }

    inline fun <O> map(
        transform: (T) -> O,
    ): NumberValues<O> = NumberValues(
        singular = singular.let(transform),
        plural = plural.let(transform),
    )
}