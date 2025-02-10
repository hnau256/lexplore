package hnau.lexplore.exercise.dto

enum class Sureness {
    Low,
    Medium,
    Height;

    companion object {

        val primary: Sureness = Medium
    }
}