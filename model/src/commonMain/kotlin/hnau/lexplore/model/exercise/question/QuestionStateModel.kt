package hnau.lexplore.model.exercise.question

sealed interface QuestionStateModel {

    data class Input(
        val input: InputModel,
    ) : QuestionStateModel

    data class Error(
        val error: ErrorModel,
    ) : QuestionStateModel
}
