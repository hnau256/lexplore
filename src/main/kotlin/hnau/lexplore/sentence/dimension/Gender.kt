package hnau.lexplore.sentence.dimension

import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
    Masculine,
    Feminine,
    Neuter,
}

@Serializable
data class GenderValues<out T>(
    val masculine: T,
    val feminine: T,
    val neuter: T,
) {

    constructor(
        all: T,
    ) : this(
        masculine = all,
        feminine = all,
        neuter = all,
    )

    operator fun get(
        gender: Gender,
    ): T = when (gender) {
        Gender.Masculine -> masculine
        Gender.Feminine -> feminine
        Gender.Neuter -> neuter
    }
}