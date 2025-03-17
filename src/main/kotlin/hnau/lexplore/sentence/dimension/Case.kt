package hnau.lexplore.sentence.dimension

import kotlinx.serialization.Serializable

@Serializable
enum class Case {
    Nominative,
    Genitive,
    Accusative,
}

@Serializable
data class CaseValues<out T>(
    val nominative: T,
    val genitive: T,
    val accusative: T,
) {

    operator fun get(
        case: Case,
    ): T = when (case) {
        Case.Nominative -> nominative
        Case.Genitive -> genitive
        Case.Accusative -> accusative
    }

    inline fun <O> map(
        transform: (T) -> O,
    ): CaseValues<O> = CaseValues(
        nominative = nominative.let(transform),
        genitive = genitive.let(transform),
        accusative = accusative.let(transform),
    )
}