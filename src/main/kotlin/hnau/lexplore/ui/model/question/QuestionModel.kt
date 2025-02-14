package hnau.lexplore.ui.model.question

import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.Question
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.ui.model.error.ErrorModel
import hnau.lexplore.ui.model.input.InputModel
import hnau.lexplore.utils.normalized
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    val question: Question,
    private val switchToNextQuestion: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val error: MutableStateFlow<ErrorModel.Skeleton?> = MutableStateFlow(null),
        var input: InputModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        fun input(): InputModel.Dependencies

        fun error(): ErrorModel.Dependencies
    }

    private val onAnswerInProgressRegistry = InProgressRegistry()

    val isAnswering: StateFlow<Boolean>
        get() = onAnswerInProgressRegistry.isProgress

    val state: StateFlow<QuestionStateModel> = skeleton
        .error
        .mapWithScope(scope) { stateScope, errorSkeleton ->
            when (errorSkeleton) {
                null -> QuestionStateModel.Input(
                    InputModel(
                        scope = stateScope,
                        skeleton = skeleton::input
                            .toAccessor()
                            .getOrInit { InputModel.Skeleton() },
                        dependencies = dependencies.input(),
                        onReady = ::onInput,
                    )
                )

                else -> QuestionStateModel.Error(
                    ErrorModel(
                        scope = stateScope,
                        skeleton = errorSkeleton,
                        dependencies = dependencies.error(),
                        onEnteredCorrect = { onAnswer(Answer.Incorrect) },
                        onTypo = ::onCorrect,
                        wordToLearn = question.word.toLearn,
                    )
                )
            }
        }

    fun onAnswer(
        answer: Answer,
    ) {
        scope.launch {
            onAnswerInProgressRegistry.executeRegistered {
                question.answer(answer)
                switchToNextQuestion()
            }
        }
    }

    private fun onInput(
        input: String,
        sureness: Sureness,
    ) {
        when (input.normalized) {
            question.word.toLearn.word.normalized -> onCorrect(
                sureness = sureness,
            )

            else -> skeleton.error.value = ErrorModel.Skeleton(
                incorrectInput = input,
                selectedSureness = sureness,
            )
        }
    }

    private fun onCorrect(
        sureness: Sureness,
    ) {
        onAnswer(
            Answer.Correct(sureness)
        )
    }

    val title: String
        get() = question.word.translation.translation
}