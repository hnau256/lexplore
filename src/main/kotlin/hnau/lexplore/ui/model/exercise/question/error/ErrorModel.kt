package hnau.lexplore.ui.model.exercise.question.error

import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.lexplore.common.kotlin.coroutines.onSet
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.exercise.AutoTTS
import hnau.lexplore.utils.TTS
import hnau.lexplore.utils.normalized
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.ifTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class ErrorModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    val wordToLearn: WordToLearn,
    private val onAnswer: (FsrsRating) -> Unit,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val tts: TTS

        val autoTTS: AutoTTS
    }

    @Serializable
    data class Skeleton(
        val incorrectInput: String,
        val defaultRating: FsrsRating = FsrsRating.Hard,
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue> =
            MutableStateFlow(TextFieldValue()),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedRating: MutableStateFlow<FsrsRating> = MutableStateFlow(defaultRating),
    )

    val incorrectInput: String
        get() = skeleton.incorrectInput

    val selectedRating: StateFlow<FsrsRating>
        get() = skeleton.selectedRating

    val input: MutableStateFlow<TextFieldValue> = skeleton
        .input
        .onSet { newValue ->
            (newValue.text.normalized == wordToLearn.word.normalized).ifTrue {
                onAnswer(skeleton.selectedRating.value)
            }
        }

    fun selectRating(
        rating: FsrsRating,
    ) {
        skeleton.selectedRating.value = rating
    }

    val correctWorkSpeaker: StateFlow<(() -> Unit)?> = actionOrNullIfExecuting(
        scope = scope,
    ) {
        dependencies.tts.speek(wordToLearn.word)
    }

    init {
        if (dependencies.autoTTS.active.value) {
            correctWorkSpeaker.value?.invoke()
        }
    }
}
