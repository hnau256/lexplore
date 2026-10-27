package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.exercise.dto.Retrievability
import hnau.lexplore.exercise.dto.Stability
import hnau.lexplore.exercise.dto.WordInfo
import org.hnau.commons.kotlin.foldBoolean
import kotlin.math.exp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

object Fsrs {

    private val parameters = FsrsParameters.Default
    private val decay = parameters.curveDecay
    private val factor = 0.9.pow(-1.0 / decay) - 1.0
    private val stabilityRegularization = Stability.from(1.days)
    private val difficultyEasy = initialDifficulty(FsrsRating.Easy)

    private const val stabilityRegularizationPow = 4.0
    private const val learningBlendStart = 0.8

    val minStability = Stability(LearningConstants.baseInterval / 1.days)

    fun retrievability(
        stability: Stability,
        elapsed: Duration,
    ): Retrievability = Retrievability(
        value = retrievabilityValue(
            stabilityDays = stability.days.coerceAtLeast(minStability.days),
            elapsedDays = (elapsed / 1.days).coerceAtLeast(0.0),
        ),
    )

    fun initialState(
        rating: FsrsRating,
        now: Instant,
    ): WordInfo = WordInfo(
        stability = initialStability(rating),
        difficulty = initialDifficulty(rating),
        lastAnswerTimestamp = now,
    )

    fun review(
        info: WordInfo,
        rating: FsrsRating,
        now: Instant,
    ): WordInfo {
        val stability = info.stability.days.coerceAtLeast(minStability.days)
        val difficulty = info.difficulty.value.coerceIn(1.0, 10.0)
        val retrievability = retrievability(
            stability = info.stability,
            elapsed = now - info.lastAnswerTimestamp,
        ).value
        val newStability = when (rating) {
            FsrsRating.Again -> lapseStability(
                stability = stability,
                difficulty = difficulty,
                retrievability = retrievability,
            )
            else -> successStability(
                stability = stability,
                difficulty = difficulty,
                retrievability = retrievability,
                rating = rating,
            )
        }.coerceAtLeast(minStability.days)
        return info.copy(
            stability = Stability(newStability),
            difficulty = Difficulty(
                nextDifficulty(
                    difficulty = difficulty,
                    rating = rating,
                )
            ),
            lastAnswerTimestamp = now,
        )
    }

    private fun retrievabilityValue(
        stabilityDays: Double,
        elapsedDays: Double,
    ): Double = (1.0 + factor * elapsedDays / stabilityDays).pow(-decay)

    private fun initialStability(
        rating: FsrsRating,
    ): Stability {
        val stability = when (rating) {
            FsrsRating.Again -> parameters.initialStabilityAgain
            FsrsRating.Hard -> parameters.initialStabilityHard
            FsrsRating.Good -> parameters.initialStabilityGood
            FsrsRating.Easy -> parameters.initialStabilityEasy
        }
        return Stability(stability.days.coerceAtLeast(minStability.days))
    }

    private fun initialDifficulty(
        rating: FsrsRating,
    ): Difficulty {
        val value = parameters.initialDifficultyAgain.value -
            exp(parameters.initialDifficultySlope * (rating.grade - 1)) + 1.0
        return Difficulty(value.coerceIn(1.0, 10.0))
    }

    private fun successStability(
        stability: Double,
        difficulty: Double,
        retrievability: Double,
        rating: FsrsRating,
    ): Double {
        val hardPenalty = (rating == FsrsRating.Hard).foldBoolean(
            ifTrue = { parameters.hardPenalty },
            ifFalse = { 1.0 },
        )
        val easyBonus = (rating == FsrsRating.Easy).foldBoolean(
            ifTrue = { parameters.easyBonus },
            ifFalse = { 1.0 },
        )
        val fsrsIncrease = 1.0 +
            exp(parameters.stabilityScale) *
            (11.0 - difficulty) *
            regularizedStability(stability).pow(-parameters.stabilityDecay) *
            (exp(parameters.retrievabilitySensitivity * (1.0 - retrievability)) - 1.0) *
            hardPenalty *
            easyBonus
        val learningHardPenalty = (rating == FsrsRating.Hard).foldBoolean(
            ifTrue = { parameters.learningHardPenalty },
            ifFalse = { 1.0 },
        )
        val learningEasyBonus = (rating == FsrsRating.Easy).foldBoolean(
            ifTrue = { parameters.learningEasyBonus },
            ifFalse = { 1.0 },
        )
        val learningIncrease = parameters.learningStabilityIncrease *
            learningHardPenalty *
            learningEasyBonus
        return stability * lerp(
            start = fsrsIncrease,
            stop = learningIncrease,
            fraction = learningWeight(stability),
        )
    }

    private fun learningWeight(
        stability: Double,
    ): Double {
        val progress = (stability / parameters.learningStabilityLimit.days)
            .coerceIn(0.0, 1.0)
        val blend = ((progress - learningBlendStart) / (1.0 - learningBlendStart))
            .coerceIn(0.0, 1.0)
        return 1.0 - smoothstep(blend)
    }

    private fun smoothstep(
        value: Double,
    ): Double {
        val x = value.coerceIn(0.0, 1.0)
        return x * x * (3.0 - 2.0 * x)
    }

    private fun lerp(
        start: Double,
        stop: Double,
        fraction: Double,
    ): Double = start + (stop - start) * fraction

    private fun lapseStability(
        stability: Double,
        difficulty: Double,
        retrievability: Double,
    ): Double = minOf(
        parameters.lapseScale *
            difficulty.pow(-parameters.lapseDifficultyDecay) *
            ((stability + 1.0).pow(parameters.lapseStabilityGrowth) - 1.0) *
            exp(parameters.lapseRetrievabilitySensitivity * (1.0 - retrievability)),
        stability,
    )

    private fun nextDifficulty(
        difficulty: Double,
        rating: FsrsRating,
    ): Double {
        val delta = -parameters.difficultyDelta * (rating.grade - 3)
        val damped = difficulty + delta * (10.0 - difficulty) / 9.0
        val reverted = parameters.difficultyMeanReversion * difficultyEasy.value +
            (1.0 - parameters.difficultyMeanReversion) * damped
        return reverted.coerceIn(1.0, 10.0)
    }

    private fun regularizedStability(
        stability: Double,
    ): Double = (
        stability.pow(stabilityRegularizationPow) +
            stabilityRegularization.days.pow(stabilityRegularizationPow)
        ).pow(1.0 / stabilityRegularizationPow)
}
