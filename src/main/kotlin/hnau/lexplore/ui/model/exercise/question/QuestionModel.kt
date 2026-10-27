package hnau.lexplore.ui.model.exercise.question

import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.ExerciseWords
import hnau.lexplore.exercise.defaultRating
import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.exercise.AutoTTS
import hnau.lexplore.ui.model.exercise.question.error.ErrorModel
import hnau.lexplore.ui.model.exercise.question.input.InputModel
import hnau.lexplore.utils.TTS
import hnau.lexplore.utils.normalized
import hnau.lexplore.utils.recallScore
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onAnswer: suspend (FsrsRating) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val wordToLearn: WordToLearn,
        @Serializable(MutableStateFlowSerializer::class)
        val previousWord: MutableStateFlow<WordToLearn?>,
        @Serializable(MutableStateFlowSerializer::class)
        val error: MutableStateFlow<ErrorModel.Skeleton?> = MutableStateFlow(null),
        var input: InputModel.Skeleton? = null,
    )

    @Pipe
    interface Dependencies {

        fun input(): InputModel.Dependencies

        fun error(): ErrorModel.Dependencies

        val exerciseWords: ExerciseWords

        val repository: KnowledgeRepository

        val tts: TTS

        val autoTTS: AutoTTS
    }

    data class PreviousWordSpeaker(
        val word: WordToLearn,
        val speak: StateFlow<(() -> Unit)?>,
        val close: () -> Unit,
    )

    val previousWordSpeaker: StateFlow<PreviousWordSpeaker?> = skeleton
        .previousWord
        .mapWithScope(scope) { previousWordScope, previousWordOrNull ->
            previousWordOrNull?.let { previousWord ->
                PreviousWordSpeaker(
                    word = previousWord,
                    speak = actionOrNullIfExecuting(
                        scope = previousWordScope,
                    ) {
                        dependencies.tts.speek(previousWord.word)
                    },
                    close = { skeleton.previousWord.value = null },
                )
            }
        }

    init {
        if (dependencies.autoTTS.active.value) {
            previousWordSpeaker
                .value
                ?.speak
                ?.value
                ?.invoke()
        }
    }

    val wordToLearn: WordToLearn
        get() = skeleton.wordToLearn

    val info: WordInfo? = dependencies
        .repository[wordToLearn]
        .value

    private val answeringInProgressRegistry = InProgressRegistry()

    val isAnswering: StateFlow<Boolean>
        get() = answeringInProgressRegistry.isProgress

    val state: StateFlow<QuestionStateModel> = skeleton
        .error
        .mapWithScope(scope) { stateScope, errorSkeleton ->
            errorSkeleton.foldNullable(
                ifNull = {
                    QuestionStateModel.Input(
                        InputModel(
                            scope = stateScope,
                            skeleton = skeleton::input
                                .toAccessor()
                                .getOrInit { InputModel.Skeleton() },
                            dependencies = dependencies.input(),
                            onGradeSelected = ::onGrade,
                        )
                    )
                },
                ifNotNull = { error ->
                    QuestionStateModel.Error(
                        ErrorModel(
                            scope = stateScope,
                            skeleton = error,
                            dependencies = dependencies.error(),
                            wordToLearn = skeleton.wordToLearn,
                            onAnswer = ::onAnswer,
                        )
                    )
                },
            )
        }

    private fun onGrade(
        rating: FsrsRating,
        input: String,
    ) {
        when (rating) {
            FsrsRating.Again -> onAnswer(rating)
            else -> {
                val target = wordToLearn.word
                (input.normalized == target.normalized).foldBoolean(
                    ifTrue = { onAnswer(rating) },
                    ifFalse = {
                        val score = input.recallScore(target)
                        skeleton.error.value = ErrorModel.Skeleton(
                            incorrectInput = input,
                            defaultRating = defaultRating(score),
                        )
                    },
                )
            }
        }
    }

    fun onAnswer(
        rating: FsrsRating,
    ) {
        scope.launch {
            answeringInProgressRegistry.executeRegistered {
                onAnswer.invoke(rating)
            }
        }
    }

    val title: String
        get() = wordToLearn
            .let(dependencies.exerciseWords::get)
            .translation
}
