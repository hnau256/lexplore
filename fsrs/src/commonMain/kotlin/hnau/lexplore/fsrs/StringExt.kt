package hnau.lexplore.fsrs

val String.normalized: String
    get() = lowercase().trim().withoutNonSpacingMarks()

internal expect fun String.withoutNonSpacingMarks(): String
