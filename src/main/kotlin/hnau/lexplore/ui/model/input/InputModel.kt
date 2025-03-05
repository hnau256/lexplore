package hnau.lexplore.ui.model.input

import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.exercise.dto.Sureness
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class InputModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onReady: (input: String, sureness: Sureness) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue> =
            MutableStateFlow(TextFieldValue()),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedSureness: MutableStateFlow<Sureness> = MutableStateFlow(Sureness.default),
    )

    val input: MutableStateFlow<TextFieldValue>
        get() = skeleton.input

    @Shuffle
    interface Dependencies

    val selectedSureness: MutableStateFlow<Sureness>
        get() = skeleton.selectedSureness

    fun onReady(
        input: String,
    ) {
        onReady(
            input,
            skeleton.selectedSureness.value,
        )
    }
}