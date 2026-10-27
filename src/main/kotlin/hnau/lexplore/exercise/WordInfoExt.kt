package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.Retrievability
import hnau.lexplore.exercise.dto.WordInfo
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import kotlin.math.ln
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

private const val maxStabilityDays = 365.0

val WordInfo.durationFromLastAnswer: Duration
    get() = Clock.System.now() - lastAnswerTimestamp

fun WordInfo.retrievability(
    now: Instant,
): Retrievability = Fsrs.retrievability(
    stability = stability,
    elapsed = now - lastAnswerTimestamp,
)

val WordInfo?.stabilityFrom0To1: Float
    get() = foldNullable(
        ifNull = { 0f },
        ifNotNull = { info ->
            val stabilityDays = info.stability.days
            val minStabilityDays = Fsrs.minStability.days
            (stabilityDays > minStabilityDays).foldBoolean(
                ifTrue = {
                    (
                        ln(stabilityDays / minStabilityDays) /
                            ln(maxStabilityDays / minStabilityDays)
                        ).toFloat().coerceIn(0f, 1f)
                },
                ifFalse = { 0f },
            )
        },
    )
