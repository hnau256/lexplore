package hnau.lexplore.ui.model.question

import hnau.lexplore.common.model.goback.GoBackHandlerProvider

sealed interface QuestionStateModel : GoBackHandlerProvider {

    data class Input(
        val input: InputModel,
    ) : QuestionStateModel, GoBackHandlerProvider by input

    data class Error(
        val error: ErrorModel,
    ) : QuestionStateModel, GoBackHandlerProvider by error
}
