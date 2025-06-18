package hnau.lexplore.ui.model.edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class EditWordModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val wordToLearn: WordToLearn,
    private val onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue>,
    ) {

        constructor(
            initialValue: ForgettingFactor,
        ) : this(
            input = MutableStateFlow(
                initialValue.factor.toString().let { factorString ->
                    TextFieldValue(
                        text = factorString,
                        selection = TextRange(0, factorString.length),
                    )
                }
            )
        )
    }

    @Pipe
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository
    }

    val input: MutableStateFlow<TextFieldValue>
        get() = skeleton.input

    private val savingInProgressRegistry = InProgressRegistry()

    fun save(): Boolean {
        val newForgettingFactor = skeleton
            .input
            .value
            .text
            .toFloatOrNull()
            ?.let(::ForgettingFactor)
            ?: return false
        scope.launch {
            savingInProgressRegistry.executeRegistered {
                dependencies.knowledgeRepository.update(
                    key = wordToLearn,
                    newForgettingFactor = newForgettingFactor,
                )
            }
            onReady()
        }
        return true
    }
}