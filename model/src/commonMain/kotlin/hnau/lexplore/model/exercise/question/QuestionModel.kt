@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.lexplore.model.exercise.question

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.exercise.ExerciseWords
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.WordInfo
import hnau.lexplore.fsrs.normalized
import hnau.lexplore.model.exercise.AutoTTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancel
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.coroutines.operationOrInProgressIfExecuting
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onAnswer: suspend (FsrsRating) -> Unit,
) {

    @SealUp(
        serializable = true,
        sealedInterfaceName = "QuestionModelStateSkeleton",
        variants = [
            Variant(
                type = InputModel.Skeleton::class,
                identifier = "input",
            ),
            Variant(
                type = ErrorModel.Skeleton::class,
                identifier = "error",
            ),
        ],
    )
    interface StateSkeleton {

        companion object
    }

    @SealUp(
        sealedInterfaceName = "QuestionModelState",
        variants = [
            Variant(
                type = InputModel::class,
                identifier = "input",
            ),
            Variant(
                type = ErrorModel::class,
                identifier = "error",
            ),
        ],
    )
    interface State {

        companion object
    }

    @Serializable
    data class Skeleton(
        val wordToLearn: WordToLearn,
        val previousWord: WordToLearn?,
        val state: MutableStateFlow<QuestionModelStateSkeleton> = StateSkeleton.input()
            .toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        val exerciseWords: ExerciseWords

        val repository: KnowledgeRepository

        val speaker: Speaker

        val autoTTS: AutoTTS

        fun error(): ErrorModel.Dependencies
    }

    val state: StateFlow<QuestionModelState> = skeleton
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifInput = { inputSkeleton ->
                    State.input(
                        scope = scope,
                        skeleton = inputSkeleton,
                        onReady = { rating, input ->
                            val target = wordToLearn.word
                            (input.normalized == target.normalized).foldBoolean(
                                ifTrue = { onAnswer(rating) },
                                ifFalse = { switchToError(input) }
                            )
                        },
                        onUnknown = { switchToError("") },
                    )
                },
                ifError = { errorSkeleton ->
                    State.error(
                        scope = scope,
                        dependencies = dependencies.error(),
                        skeleton = errorSkeleton,
                        wordToLearn = wordToLearn,
                        answer = operationOrInProgressIfExecuting(scope) { _, rating ->
                            onAnswer(rating)
                        }
                    )
                }
            )
        }

    private fun switchToError(
        input: String,
    ) {
        skeleton.state.value = StateSkeleton.error(
            incorrectInput = input
        )
    }

    data class PreviousWordSpeaker(
        val word: WordToLearn,
        val speak: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>>,
    )

    val previousWordSpeaker: PreviousWordSpeaker? = skeleton
        .previousWord
        .foldNullable(
            ifNull = { null },
            ifNotNull = { previousWord ->
                PreviousWordSpeaker(
                    word = previousWord,
                    speak = actionOrCancelIfExecuting(
                        scope = scope,
                    ) {
                        dependencies.speaker.speak(previousWord.word)
                    },
                )
            }
        )

    init {
        previousWordSpeaker
            ?.takeIf { dependencies.autoTTS.active.value }
            ?.speak
            ?.value
            ?.actionOrCancel()
    }

    val wordToLearn: WordToLearn
        get() = skeleton.wordToLearn

    val info: StateFlow<WordInfo?> =
        dependencies.repository[wordToLearn]

    val title: String
        get() = wordToLearn
            .let(dependencies.exerciseWords::get)
            .translation
}
