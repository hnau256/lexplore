package hnau.lexplore.exercise.dto

import kotlin.time.Instant

data class WordInfo(
    val forgettingFactor: ForgettingFactor,
    val lastAnswerTimestamp: Instant,
)