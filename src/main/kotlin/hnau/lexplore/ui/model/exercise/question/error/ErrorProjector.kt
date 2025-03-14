package hnau.lexplore.ui.model.exercise.question.error

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.R
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
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class ErrorProjector(
    private val scope: CoroutineScope,
    private val model: ErrorModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            Text(
                text = stringResource(R.string.question_incorrect) + ":  " + model.incorrectInput,
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
                    onClick = { model.onTypo(model.selectedSureness) },
                    shape = HnauShape.inRow.start,
                )
                SpeakButton(
                    shape = HnauShape.inRow.end,
                )
            }
            Text(
                text = model.wordToLearn.word,
                style = MaterialTheme.typography.headlineMedium,
            )
            val focusRequester = remember { FocusRequester() }
            TextInput(
                value = model.input,
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
        val speakOrNull by model.correctWorkSpeaker.collectAsState()
        Chip(
            style = ChipStyle.chipSelected,
            leading = chipInProgressLeadingContent(
                inProgress = speakOrNull == null,
            ),
            onClick = speakOrNull,
            content = { Icon { RecordVoiceOver } },
            shape = shape,
        )
    }
}