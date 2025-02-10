package hnau.lexplore.utils

import java.text.Normalizer

val String.withoutNonSpacingMarks: String
    get() = Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter {char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }

val String.normalized: String
    get() = lowercase().trim().withoutNonSpacingMarks