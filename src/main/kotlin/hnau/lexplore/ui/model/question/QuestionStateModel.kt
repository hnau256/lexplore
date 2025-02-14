package hnau.lexplore.ui.model.question

import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.ui.model.error.ErrorModel
import hnau.lexplore.ui.model.input.InputModel

sealed interface QuestionStateModel : GoBackHandlerProvider {

    data class Input(
        val input: InputModel,
    ) : QuestionStateModel, GoBackHandlerProvider by input

    data class Error(
        val error: ErrorModel,
    ) : QuestionStateModel, GoBackHandlerProvider by error
}
