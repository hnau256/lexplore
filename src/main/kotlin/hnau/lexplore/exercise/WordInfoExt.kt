package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.KnowLevel
import hnau.lexplore.exercise.dto.WordInfo
import kotlinx.datetime.Clock
import kotlin.math.exp
import kotlin.math.pow

val WordInfo?.knowLevel: KnowLevel
    get() {
        val now = Clock.System.now()
        val info = this ?: WordInfo(
            forgettingFactor = LearningConstants.initialForgettingFactor,
            lastAnswerTimestamp = now - LearningConstants.newWordFakeLastAnswerBefore
        )
        val durationFromLastAnswer = now - info.lastAnswerTimestamp
        val timeFactor = durationFromLastAnswer / LearningConstants.baseInterval
        val result = exp(-timeFactor / info.forgettingFactor.factor)
        return KnowLevel(
            level = result.toFloat(),
        )
    }

val WordInfo?.weight: Float
    get() = (1f / knowLevel.level - 1)
        .coerceAtLeast(0f)
        .pow(LearningConstants.weightPow)