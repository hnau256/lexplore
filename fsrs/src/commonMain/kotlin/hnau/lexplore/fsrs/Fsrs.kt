package hnau.lexplore.fsrs

import hnau.lexplore.fsrs.dto.Difficulty
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.fold
import hnau.lexplore.fsrs.dto.Retrievability
import hnau.lexplore.fsrs.dto.Stability
import hnau.lexplore.fsrs.dto.WordInfo
import org.hnau.commons.kotlin.foldBoolean
import kotlin.math.exp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

object Fsrs {

    private val parameters = FsrsParameters.Default
    private val decay = parameters.curveDecay
    private val factor = parameters.stabilityReferenceRetention.value
        .pow(-1.0 / decay) - 1.0
    private val difficultyEasy = initialDifficulty(FsrsRating.Easy)

    val minStability = Stability(1.minutes)

    fun retrievability(
        stability: Stability,
        elapsed: Duration,
    ): Retrievability {
        val stabilityDays = stability.days.coerceAtLeast(minStability.days)
        val elapsedDays = (elapsed / 1.days).coerceAtLeast(0.0)
        return Retrievability(
            (1.0 + factor * elapsedDays / stabilityDays).pow(-decay),
        )
    }

    fun initialState(
        rating: FsrsRating,
        now: Instant,
    ): WordInfo = WordInfo(
        stability = parameters.initialStability[rating].coerceAtLeast(minStability),
        difficulty = initialDifficulty(rating),
        lastAnswerTimestamp = now,
    )

    fun review(
        info: WordInfo,
        rating: FsrsRating,
        now: Instant,
    ): WordInfo {
        val stability = info.stability.coerceAtLeast(minStability)
        val difficulty = info.difficulty.coerceIn(Difficulty.Range)
        val retrievability = retrievability(
            stability = stability,
            elapsed = now - info.lastAnswerTimestamp,
        )
        val newStability = rating.fold(
            ifAgain = {
                lapseStability(
                    stability = stability,
                    difficulty = difficulty,
                    retrievability = retrievability,
                )
            },
            ifHard = {
                successStability(
                    stability = stability,
                    difficulty = difficulty,
                    retrievability = retrievability,
                    rating = rating,
                )
            },
            ifGood = {
                successStability(
                    stability = stability,
                    difficulty = difficulty,
                    retrievability = retrievability,
                    rating = rating,
                )
            },
            ifEasy = {
                successStability(
                    stability = stability,
                    difficulty = difficulty,
                    retrievability = retrievability,
                    rating = rating,
                )
            },
        ).coerceAtLeast(minStability)
        return info.copy(
            stability = newStability,
            difficulty = nextDifficulty(
                difficulty = difficulty,
                rating = rating,
            ),
            lastAnswerTimestamp = now,
        )
    }

    private fun initialDifficulty(
        rating: FsrsRating,
    ): Difficulty {
        val value = parameters.initialDifficultyAgain.value -
            exp(parameters.initialDifficultySlope * (rating.grade - 1)) + 1.0
        return Difficulty(value).coerceIn(Difficulty.Range)
    }

    private fun successStability(
        stability: Stability,
        difficulty: Difficulty,
        retrievability: Retrievability,
        rating: FsrsRating,
    ): Stability {
        val hardPenalty = (rating == FsrsRating.Hard).foldBoolean(
            ifTrue = { parameters.hardPenalty },
            ifFalse = { 1.0 },
        )
        val easyBonus = (rating == FsrsRating.Easy).foldBoolean(
            ifTrue = { parameters.easyBonus },
            ifFalse = { 1.0 },
        )
        val stabilityDays = stability.days.coerceAtLeast(minStability.days)
        val stabilityIncrease = 1.0 +
            exp(parameters.stabilityScale) *
            (Difficulty.Max.value + 1.0 - difficulty.value) *
            stabilityDays.pow(-parameters.stabilityDecay) *
            (exp(parameters.retrievabilitySensitivity * retrievability.complement.value) - 1.0) *
            hardPenalty *
            easyBonus
        return stability * stabilityIncrease
    }

    private fun lapseStability(
        stability: Stability,
        difficulty: Difficulty,
        retrievability: Retrievability,
    ): Stability {
        val clampedStability = stability.coerceAtLeast(minStability)
        val stabilityWithStep = clampedStability + 1.days
        val value = parameters.lapseScale *
            difficulty.value.pow(-parameters.lapseDifficultyDecay) *
            (stabilityWithStep.days.pow(parameters.lapseStabilityGrowth) - 1.0) *
            exp(parameters.lapseRetrievabilitySensitivity * retrievability.complement.value)
        return Stability(value.days).coerceAtMost(clampedStability)
    }

    private fun nextDifficulty(
        difficulty: Difficulty,
        rating: FsrsRating,
    ): Difficulty {
        val delta = -parameters.difficultyDelta * (rating.grade - 3)
        val damped = difficulty +
            (Difficulty.Max - difficulty) * delta / Difficulty.Span
        val reverted = difficultyEasy * parameters.difficultyMeanReversion +
            damped * (1.0 - parameters.difficultyMeanReversion)
        return reverted.coerceIn(Difficulty.Range)
    }
}
