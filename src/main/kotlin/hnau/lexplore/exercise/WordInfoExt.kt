package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.KnowLevel
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.forgettingFactor
import kotlinx.datetime.Clock
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration

val WordInfo.durationFromLastAnswer: Duration
    get() = Clock.System.now() - lastAnswerTimestamp

val WordInfo?.knowLevel: KnowLevel
    get() {
        val now = Clock.System.now()
        val info = this ?: WordInfo(
            forgettingFactor = forgettingFactor,
            lastAnswerTimestamp = now - LearningConstants.newWordFakeLastAnswerBefore
        )
        val timeFactor = info.durationFromLastAnswer / LearningConstants.baseInterval
        val result = exp(-timeFactor / info.forgettingFactor.factor)
        return KnowLevel(
            level = result.toFloat(),
        )
    }

val WordInfo?.weight: Float
    get() = (1f / knowLevel.level - 1)
        .coerceAtLeast(0f)
        .pow(LearningConstants.weightPow)

val WordInfo?.forgettingFactorFrom0To1: Float
    get() {
        val x = forgettingFactor.factor - 1
        val k = -ln(2.0) / (500 - 1)
        return 1f - exp(k * x).toFloat()
    }