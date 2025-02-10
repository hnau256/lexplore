package hnau.lexplore.ui.model.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.R
import hnau.lexplore.common.kotlin.coroutines.onSet
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.progressindicator.chipInProgressLeadingContent
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.end
import hnau.lexplore.common.ui.uikit.shape.inRow
import hnau.lexplore.common.ui.uikit.shape.start
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.dto.WordToLearn
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
    private val wordToLearn: WordToLearn,
    private val onTypo: (sureness: Sureness) -> Unit,
    private val onEnteredCorrect: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val incorrectInput: String,
        val selectedSureness: Sureness,
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue> =
            MutableStateFlow(TextFieldValue()),
    )

    private val input: MutableStateFlow<TextFieldValue> = skeleton
        .input
        .onSet { newValue ->
            if (newValue.text.normalized == wordToLearn.word.normalized) {
                onEnteredCorrect()
            }
        }

    @Shuffle
    interface Dependencies {

        val tts: TTS
    }

    @Shuffle
    interface ContentDependencies

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            Text(
                text = stringResource(R.string.question_incorrect) + ":  " + skeleton.incorrectInput,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.chipsSeparation),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Chip(
                    content = { Text(text = stringResource(R.string.question_typo)) },
                    activeColor = MaterialTheme.colorScheme.error,
                    style = ChipStyle.button,
                    onClick = { onTypo(skeleton.selectedSureness) },
                    shape = HnauShape.inRow.start,
                )
                SpeakButton(
                    shape = HnauShape.inRow.end,
                )
            }
            Text(
                text = wordToLearn.word,
                style = MaterialTheme.typography.headlineMedium,
            )
            val focusRequester = remember { FocusRequester() }
            TextInput(
                value = input,
                shape = HnauShape(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                keyboardActions = KeyboardActions {},
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
        }
    }

    @Composable
    private fun SpeakButton(
        shape: Shape = HnauShape(),
    ) {
        var speakNumber by remember { mutableIntStateOf(0) }
        var isSpeaking by remember { mutableStateOf(false) }
        val tts = dependencies.tts
        val word = wordToLearn.word
        LaunchedEffect(speakNumber, tts, word) {
            if (speakNumber > 0) {
                isSpeaking = true
                tts.speek(word)
                isSpeaking = false
            }
        }
        Chip(
            style = ChipStyle.chipSelected,
            leading = chipInProgressLeadingContent(
                inProgress = isSpeaking,
            ),
            onClick = when (isSpeaking) {
                true -> null
                false -> {
                    { speakNumber++ }
                }
            },
            content = { Icon { RecordVoiceOver } },
            shape = shape,
        )
    }
}