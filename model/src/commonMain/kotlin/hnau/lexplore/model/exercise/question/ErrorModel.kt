package hnau.lexplore.model.exercise.question

import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.normalized
import hnau.lexplore.model.exercise.AutoTTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancel
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class ErrorModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    val wordToLearn: WordToLearn,
    private val onAnswer: (FsrsRating) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val speaker: Speaker

        val autoTTS: AutoTTS
    }

    @Serializable
    data class Skeleton(
        val incorrectInput: String,
        val defaultRating: FsrsRating = FsrsRating.Hard,
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<String> = "".toMutableStateFlowAsInitial(),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedRating: MutableStateFlow<FsrsRating> = defaultRating.toMutableStateFlowAsInitial(),
    )

    val incorrectInput: String
        get() = skeleton.incorrectInput

    val selectedRating: StateFlow<FsrsRating>
        get() = skeleton.selectedRating

    val input: MutableStateFlow<String>
        get() = skeleton.input

    init {
        scope.launch {
            skeleton.input.collect { newValue ->
                (newValue.normalized == wordToLearn.word.normalized).ifTrue {
                    onAnswer(skeleton.selectedRating.value)
                }
            }
        }
    }

    fun selectRating(
        rating: FsrsRating,
    ) {
        skeleton.selectedRating.value = rating
    }

    val correctWordSpeaker: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(scope) {
            dependencies.speaker.speak(wordToLearn.word)
        }

    init {
        dependencies.autoTTS.active.value.ifTrue {
            correctWordSpeaker.value.actionOrCancel()
        }
    }
}
