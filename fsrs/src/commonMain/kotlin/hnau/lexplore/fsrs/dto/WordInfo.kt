package hnau.lexplore.fsrs.dto

import kotlin.time.Instant

data class WordInfo(
    val stability: Stability,
    val difficulty: Difficulty,
    val lastAnswerTimestamp: Instant,
)
