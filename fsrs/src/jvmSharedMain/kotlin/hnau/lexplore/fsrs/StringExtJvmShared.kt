package hnau.lexplore.fsrs

import java.text.Normalizer

internal actual fun String.withoutNonSpacingMarks(): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .filter { char -> Character.getType(char) != Character.NON_SPACING_MARK.toInt() }
