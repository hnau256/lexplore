package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.Stability
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal data class FsrsParameters(
    val initialStabilityAgain: Stability,
    val initialStabilityHard: Stability,
    val initialStabilityGood: Stability,
    val initialStabilityEasy: Stability,
    val initialDifficultyAgain: Difficulty,
    val initialDifficultySlope: Double,
    val difficultyDelta: Double,
    val difficultyMeanReversion: Double,
    val stabilityScale: Double,
    val stabilityDecay: Double,
    val retrievabilitySensitivity: Double,
    val lapseScale: Double,
    val lapseDifficultyDecay: Double,
    val lapseStabilityGrowth: Double,
    val lapseRetrievabilitySensitivity: Double,
    val hardPenalty: Double,
    val easyBonus: Double,
    val learningStabilityIncrease: Double,
    val learningHardPenalty: Double,
    val learningEasyBonus: Double,
    val learningStabilityLimit: Stability,
    val curveDecay: Double,
) {

    companion object {

        val Default = FsrsParameters(
            initialStabilityAgain = Stability.from(0.212.minutes),
            initialStabilityHard = Stability.from(1.2931.minutes),
            initialStabilityGood = Stability.from(2.3065.minutes),
            initialStabilityEasy = Stability.from(8.2956.minutes),
            initialDifficultyAgain = Difficulty(6.4133),
            initialDifficultySlope = 0.8334,
            difficultyDelta = 3.0194,
            difficultyMeanReversion = 0.001,
            stabilityScale = 1.8722,
            stabilityDecay = 0.1666,
            retrievabilitySensitivity = 0.796,
            lapseScale = 1.4835,
            lapseDifficultyDecay = 0.0614,
            lapseStabilityGrowth = 0.2629,
            lapseRetrievabilitySensitivity = 1.6483,
            hardPenalty = 0.6014,
            easyBonus = 1.8729,
            learningStabilityIncrease = 1.9,
            learningHardPenalty = 0.9,
            learningEasyBonus = 1.2,
            learningStabilityLimit = Stability.from(1.days),
            curveDecay = 0.1542,
        )
    }
}
