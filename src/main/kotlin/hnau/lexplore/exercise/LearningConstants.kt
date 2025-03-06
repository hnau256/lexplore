package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.KnowLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object LearningConstants {

    val newWordFakeLastAnswerBefore: Duration = 10.days

    val baseInterval: Duration = 1.minutes

    val uselessForgettingFactor: ForgettingFactor = ForgettingFactor(10000000f)

    val almostKnownForgettingFactor: ForgettingFactor = ForgettingFactor(500f)

    const val correctForgettingFactorFactor: Float = 1.75f

    const val incorrectForgettingFactorFactor: Float = 0.5f

    val initialForgettingFactor = ForgettingFactor(1f)

    const val weightPow: Float = 5f

    val minForgettingFactor: ForgettingFactor = ForgettingFactor(1f)
}