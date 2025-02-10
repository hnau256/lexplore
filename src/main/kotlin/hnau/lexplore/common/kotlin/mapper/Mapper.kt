package hnau.lexplore.common.kotlin.mapper

data class Mapper<I, O>(
    val direct: (I) -> O,
    val reverse: (O) -> I,
) {
    companion object
}
