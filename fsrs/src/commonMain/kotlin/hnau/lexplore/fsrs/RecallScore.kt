package hnau.lexplore.fsrs

import kotlin.jvm.JvmInline

import kotlin.math.exp

private const val correctnessSoftness = 0.5
private val worstCorrectness = exp(-1.0 / correctnessSoftness)

@JvmInline
value class EditDistance(
    val value: Int,
) : Comparable<EditDistance> {

    override fun compareTo(
        other: EditDistance,
    ): Int = value.compareTo(other.value)
}

@JvmInline
value class WordLength(
    val value: Int,
) : Comparable<WordLength> {

    override fun compareTo(
        other: WordLength,
    ): Int = value.compareTo(other.value)
}

@JvmInline
value class Correctness(
    val value: Double,
) : Comparable<Correctness> {

    override fun compareTo(
        other: Correctness,
    ): Int = value.compareTo(other.value)
}

data class RecallScore(
    val distance: EditDistance,
    val maxLength: WordLength,
    val correctness: Correctness,
)

fun String.recallScore(
    target: String,
): RecallScore {
    val source = normalized
    val destination = target.normalized
    val maxLength = WordLength(maxOf(source.length, destination.length).coerceAtLeast(1))
    val distance = EditDistance(levenshteinDistance(source, destination))
    val raw = exp(-distance.value.toDouble() / (correctnessSoftness * maxLength.value))
    return RecallScore(
        distance = distance,
        maxLength = maxLength,
        correctness = Correctness(
            ((raw - worstCorrectness) / (1.0 - worstCorrectness)).coerceIn(0.0, 1.0),
        ),
    )
}

private fun levenshteinDistance(
    a: String,
    b: String,
): Int {
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var previous = IntArray(b.length + 1) { index -> index }
    var current = IntArray(b.length + 1)
    a.forEachIndexed { aIndex, aChar ->
        current[0] = aIndex + 1
        b.forEachIndexed { bIndex, bChar ->
            val substitutionCost = if (aChar == bChar) 0 else 1
            current[bIndex + 1] = minOf(
                current[bIndex] + 1,
                previous[bIndex + 1] + 1,
                previous[bIndex] + substitutionCost,
            )
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[b.length]
}
