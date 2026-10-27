package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.Retention
import hnau.lexplore.exercise.dto.Stability
import kotlin.time.Duration.Companion.minutes

internal data class FsrsParameters(
    val initialStabilityAgain: Stability,
    val initialStabilityHard: Stability,
    val initialStabilityGood: Stability,
    val initialStabilityEasy: Stability,
    val stabilityReferenceRetention: Retention,
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
    val curveDecay: Double,
) {

    companion object {

        val Default = FsrsParameters(
            // canonical FSRS-6: 0.212 days
            initialStabilityAgain = Stability(0.212.minutes),
            // canonical FSRS-6: 1.2931 days
            initialStabilityHard = Stability(1.2931.minutes),
            // canonical FSRS-6: 2.3065 days
            initialStabilityGood = Stability(2.3065.minutes),
            // canonical FSRS-6: 8.2956 days
            initialStabilityEasy = Stability(8.2956.minutes),
            stabilityReferenceRetention = Retention(0.9),
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
            curveDecay = 0.1542,
        )
    }
}
