package hnau.lexplore.exercise.dto

import kotlinx.datetime.Instant

data class WordInfo(
    val forgettingFactor: ForgettingFactor,
    val lastAnswerTimestamp: Instant,
)