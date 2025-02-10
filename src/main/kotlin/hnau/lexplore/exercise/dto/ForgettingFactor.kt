package hnau.lexplore.exercise.dto

data class ForgettingFactor(
    val factor: Float,
) : Comparable<ForgettingFactor> {

    private inline fun map(
        transform: (Float) -> Float,
    ): ForgettingFactor = ForgettingFactor(
        factor = transform(factor),
    )

    operator fun times(factor: Number): ForgettingFactor =
        map { it * factor.toFloat() }

    override fun compareTo(
        other: ForgettingFactor,
    ): Int = factor.compareTo(
        other = other.factor,
    )
}