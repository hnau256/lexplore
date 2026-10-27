package hnau.lexplore.common.exercise

import hnau.lexplore.common.Translation
import hnau.lexplore.common.WordToLearn

data class ExerciseWords(
    private val words: Map<WordToLearn, Translation>,
) {

    operator fun get(
        word: WordToLearn,
    ): Translation = words.getValue(
        key = word
    )
}
