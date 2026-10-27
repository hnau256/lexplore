package hnau.lexplore.ui.model.exercise.question

import androidx.annotation.StringRes
import hnau.lexplore.R
import hnau.lexplore.exercise.dto.FsrsRating

@get:StringRes
val FsrsRating.labelRes: Int
    get() = when (this) {
        FsrsRating.Again -> R.string.question_grade_again
        FsrsRating.Hard -> R.string.question_grade_hard
        FsrsRating.Good -> R.string.question_grade_good
        FsrsRating.Easy -> R.string.question_grade_easy
    }
