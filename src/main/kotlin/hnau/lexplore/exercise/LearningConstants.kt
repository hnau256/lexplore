package hnau.lexplore.exercise

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object LearningConstants {

    val baseInterval: Duration = 1.minutes

    const val priorityPow: Double = 2.0

    const val newWordProbabilityK: Double = 0.05

    const val newWordRetentionPow: Double = 4.0

    const val softminTau: Double = 0.05

    const val previousWordPenalty: Double = 0.1
}
