package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Translation(
    val translation: String,
)