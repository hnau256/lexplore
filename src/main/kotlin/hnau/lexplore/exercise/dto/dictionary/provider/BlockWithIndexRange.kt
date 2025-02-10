package hnau.lexplore.exercise.dto.dictionary.provider

import androidx.compose.ui.util.lerp

data class BlockWithIndexRange<out T>(
    val minIndex: Float,
    val maxIndex: Float,
    val values: List<T>,
) {

    fun build(): List<Pair<Float, T>> {
        val lastValue = values.lastIndex.takeIf { it > 0 }?.toFloat()
        return values.mapIndexed { i, value ->
            val index = lerp(
                start = minIndex,
                stop = maxIndex,
                fraction = lastValue?.let { i / it } ?: 0f,
            )
            index to value
        }
    }
}