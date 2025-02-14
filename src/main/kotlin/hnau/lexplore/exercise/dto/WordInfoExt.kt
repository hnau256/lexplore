package hnau.lexplore.exercise.dto

import hnau.lexplore.exercise.LearningConstants

val WordInfo?.forgettingFactor: ForgettingFactor
    get() = this
        ?.forgettingFactor
        ?: LearningConstants.initialForgettingFactor