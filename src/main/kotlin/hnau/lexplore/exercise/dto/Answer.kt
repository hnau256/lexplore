package hnau.lexplore.exercise.dto

sealed interface Answer {

    data class Correct(
        val sureness: Sureness,
    ) : Answer

    data object Incorrect : Answer

    data object AlmostKnown : Answer

    data object Useless : Answer
}