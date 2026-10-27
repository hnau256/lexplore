package hnau.lexplore.model.exercise.question

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.exercise.ExerciseWords
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.fsrs.defaultRating
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.WordInfo
import hnau.lexplore.fsrs.normalized
import hnau.lexplore.fsrs.recallScore
import hnau.lexplore.model.exercise.AutoTTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.commons.kotlin.coroutines.actionOrCancel
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onAnswer: suspend (FsrsRating) -> Unit,
) {

    @Serializable
    data class Skeleton(
        val wordToLearn: WordToLearn,
        @Serializable(MutableStateFlowSerializer::class)
        val previousWord: MutableStateFlow<WordToLearn?> = MutableStateFlow(null),
        @Serializable(MutableStateFlowSerializer::class)
        val error: MutableStateFlow<ErrorModel.Skeleton?> = MutableStateFlow(null),
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<String> = MutableStateFlow(""),
    )

    @Pipe
    interface Dependencies {

        val exerciseWords: ExerciseWords

        val repository: KnowledgeRepository

        val speaker: Speaker

        val autoTTS: AutoTTS

        fun input(): InputModel.Dependencies

        fun error(): ErrorModel.Dependencies
    }

    data class PreviousWordSpeaker(
        val word: WordToLearn,
        val speak: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>>,
        val close: () -> Unit,
    )

    val previousWordSpeaker: StateFlow<PreviousWordSpeaker?> = skeleton
        .previousWord
        .mapWithScope(scope) { previousWordScope, previousWordOrNull ->
            previousWordOrNull?.let { previousWord ->
                PreviousWordSpeaker(
                    word = previousWord,
                    speak = actionOrCancelIfExecuting(
                        scope = previousWordScope,
                    ) {
                        dependencies.speaker.speak(previousWord.word)
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
                ?.actionOrCancel()
        }
    }

    val wordToLearn: WordToLearn
        get() = skeleton.wordToLearn

    val info: WordInfo? = dependencies
        .repository[wordToLearn]
        .value

    val input: MutableStateFlow<String>
        get() = skeleton.input

    private val answeringInProgressRegistry = InProgressRegistry(scope)

    val isAnswering: StateFlow<Boolean>
        get() = answeringInProgressRegistry.inProgress

    val state: StateFlow<QuestionStateModel> = skeleton
        .error
        .mapWithScope(scope) { stateScope, errorSkeleton ->
            errorSkeleton.foldNullable(
                ifNull = {
                    QuestionStateModel.Input(
                        InputModel(
                            scope = stateScope,
                            input = skeleton.input,
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
