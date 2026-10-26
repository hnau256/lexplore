package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.KnowLevel
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.forgettingFactor
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.math.exp
import kotlin.math.ln

val WordInfo.durationFromLastAnswer: Duration
    get() = Clock.System.now() - lastAnswerTimestamp

fun WordInfo.knowLevel(
    now: Instant,
): KnowLevel {
    val timeFactor = (now - lastAnswerTimestamp) / LearningConstants.baseInterval
    val result = exp(-timeFactor / forgettingFactor.factor.toDouble())
    return KnowLevel(
        level = result.toFloat(),
    )
}

fun WordInfo.overdueRatio(
    now: Instant,
): Double {
    val timeFactor = (now - lastAnswerTimestamp) / LearningConstants.baseInterval
    return timeFactor / forgettingFactor.factor.toDouble()
}

val WordInfo?.knowLevel: KnowLevel
    get() {
        val now = Clock.System.now()
        val info = this ?: WordInfo(
            forgettingFactor = forgettingFactor,
            lastAnswerTimestamp = now - LearningConstants.newWordFakeLastAnswerBefore
        )
        return info.knowLevel(now)
    }

val WordInfo?.forgettingFactorFrom0To1: Float
    get() {
        val x = forgettingFactor.factor - 1
        val k = -ln(2.0) / (500 - 1)
        return 1f - exp(k * x).toFloat()
    }
