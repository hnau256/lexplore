package hnau.lexplore.exercise

import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.utils.Correctness
import hnau.lexplore.utils.EditDistance
import hnau.lexplore.utils.RecallScore
import hnau.lexplore.utils.WordLength
import org.hnau.commons.kotlin.foldBoolean

private val singleTypoMinLength = WordLength(4)
private val againCorrectness = Correctness(0.3)

fun defaultRating(
    score: RecallScore,
): FsrsRating {
    val isTypo = score.distance == EditDistance(1) && score.maxLength >= singleTypoMinLength
    return isTypo.foldBoolean(
        ifTrue = { FsrsRating.Good },
        ifFalse = {
            (score.correctness < againCorrectness).foldBoolean(
                ifTrue = { FsrsRating.Again },
                ifFalse = { FsrsRating.Hard },
            )
        },
    )
}
