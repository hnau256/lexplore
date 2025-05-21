package hnau.lexplore.ui.model.exercise.question.input

import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class InputModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onReady: (input: String) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue> =
            MutableStateFlow(TextFieldValue()),
    )

    val input: MutableStateFlow<TextFieldValue>
        get() = skeleton.input

    val done: StateFlow<(() -> Unit)?> = input.mapState(scope) { textFieldValue ->
        textFieldValue
            .text
            .takeIf(String::isNotEmpty)
            ?.let { input -> { onReady(input) } }
    }

    @Shuffle
    interface Dependencies
}