package hnau.lexplore.light.engine

data class LearningWord(
    val isNew: Boolean,
    val words: List<Variant>,
    val correctWordIndex: Int,
) {

    data class Variant(
        val greek: String,
        val russian: String,
    )

    val correct: Variant
        get() = words[correctWordIndex]
}