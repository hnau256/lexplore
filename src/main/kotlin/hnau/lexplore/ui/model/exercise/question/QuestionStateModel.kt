package hnau.lexplore.ui.model.exercise.question

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.ui.model.exercise.question.error.ErrorModel
import hnau.lexplore.ui.model.exercise.question.input.InputModel

sealed interface QuestionStateModel : GoBackHandlerProvider {

    data class Input(
        val input: InputModel,
    ) : QuestionStateModel, GoBackHandlerProvider by input

    data class Error(
        val error: ErrorModel,
    ) : QuestionStateModel, GoBackHandlerProvider by error
}
