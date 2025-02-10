package hnau.lexplore.ui.model

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.bubble.Bubble
import hnau.lexplore.common.ui.uikit.bubble.BubblesShower
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.Word
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class EditWordModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val word: Word,
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

    @Shuffle
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository
    }

    @Shuffle
    interface ContentDependencies {

        val bubblesShower: BubblesShower
    }

    private val savingInProgressRegistry = InProgressRegistry()

    private fun save(): Boolean {
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
                    key = word.toLearn,
                    newForgettingFactor = newForgettingFactor,
                )
            }
            onReady()
        }
        return true
    }

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        val context = LocalContext.current
        val focusRequester = remember { FocusRequester() }
        TextInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.separation)
                .focusRequester(focusRequester),
            value = skeleton.input,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                val saveResult = save()
                if (!saveResult) {
                    dependencies.bubblesShower.showBubble(
                        Bubble(
                            text = context.getString(R.string.edit_incorrect_number)
                        )
                    )
                }
            }
        )
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }
}