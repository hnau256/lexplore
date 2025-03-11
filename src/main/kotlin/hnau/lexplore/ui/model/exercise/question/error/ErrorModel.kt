package hnau.lexplore.ui.model.exercise.question.error

import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.coroutines.onSet
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.exercise.AutoTTS
import hnau.lexplore.utils.TTS
import hnau.lexplore.utils.normalized
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class ErrorModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    val wordToLearn: WordToLearn,
    val onTypo: (sureness: Sureness) -> Unit,
    private val onEnteredCorrect: () -> Unit,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val tts: TTS

        val autoTTS: AutoTTS
    }

    @Deprecated("Play with model. Do not use TTS direct")
    val tts: TTS
        get() = dependencies.tts

    @Serializable
    data class Skeleton(
        val incorrectInput: String,
        val selectedSureness: Sureness,
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue> =
            MutableStateFlow(TextFieldValue()),
    )

    val incorrectInput: String
        get() = skeleton.incorrectInput

    val selectedSureness: Sureness
        get() = skeleton.selectedSureness

    val input: MutableStateFlow<TextFieldValue> = skeleton
        .input
        .onSet { newValue ->
            if (newValue.text.normalized == wordToLearn.word.normalized) {
                onEnteredCorrect()
            }
        }
}