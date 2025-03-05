package hnau.lexplore.exercise.dto

import kotlinx.serialization.Serializable

@Serializable
enum class Sureness {
    Low,
    Medium,
    Height;

    companion object {

        val default: Sureness = Medium
    }
}