package hnau.lexplore.sentence.dimension

import kotlinx.serialization.Serializable

@Serializable
enum class NEnding {
    WithN,
    WithoutN,
}

@Serializable
data class NEndingValues<out T>(
    val withN: T,
    val withoutN: T,
) {

    operator fun get(
        nEnding: NEnding,
    ): T = when (nEnding) {
        NEnding.WithN -> withN
        NEnding.WithoutN -> withoutN
    }
}