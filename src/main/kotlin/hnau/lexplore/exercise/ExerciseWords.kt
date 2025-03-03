package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.WordToLearn

data class ExerciseWords(
    private val words: Map<WordToLearn, Translation>,
) {

    operator fun get(
        word: WordToLearn,
    ): Translation = words.getValue(
        key = word
    )
}