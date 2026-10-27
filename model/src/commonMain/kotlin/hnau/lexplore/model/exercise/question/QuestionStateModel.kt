package hnau.lexplore.model.exercise.question

import org.hnau.commons.gen.fold.annotations.Fold

@Fold
sealed interface QuestionStateModel {

    data class Input(
        val input: InputModel,
    ) : QuestionStateModel

    data class Error(
        val error: ErrorModel,
    ) : QuestionStateModel
}
