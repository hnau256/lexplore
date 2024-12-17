package hnau.lexplore.light

import java.text.Normalizer

val String.withoutNonSpacingMarks: String
    get() = Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter {char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }