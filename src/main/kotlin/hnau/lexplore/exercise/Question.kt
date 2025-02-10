package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordInfo

data class Question(
    val word: Word,
    val info: WordInfo?,
    val answer: suspend (Answer) -> Unit,
)