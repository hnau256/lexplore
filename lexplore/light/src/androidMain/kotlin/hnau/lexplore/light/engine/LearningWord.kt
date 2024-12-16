package hnau.lexplore.light.engine

data class LearningWord(
    val isNew: Boolean,
    val translation: String,
    val words: List<String>,
    val correctWordIndex: Int,
)