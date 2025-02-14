package hnau.lexplore.ui.model.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.row.ChipsRow
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.exercise.dto.Sureness
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class InputProjector(
    private val scope: CoroutineScope,
    private val model: InputModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        Column(
            modifier = Modifier.horizontalDisplayPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            val focusRequester = remember { FocusRequester() }
            TextInput(
                shape = HnauShape(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                keyboardActions = KeyboardActions {},
                maxLines = 1,
                value = model.input,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
            ChipsRow(
                all = surenessList,
                modifier = Modifier.align(Alignment.End),
            ) { sureness, shape ->
                Chip(
                    modifier = Modifier,
                    content = { Text(sureness.name) },
                    shape = shape,
                    style = when (sureness) {
                        Sureness.primary -> ChipStyle.button
                        else -> ChipStyle.chip
                    },
                    onClick = { model.onReady(model.input.value.text, sureness) },
                )
            }
        }
    }

    companion object {

        private val surenessList: List<Sureness> = Sureness
            .entries
            .sortedBy { sureness ->
                when (sureness) {
                    Sureness.primary -> Sureness.entries.size
                    else -> sureness.ordinal
                }
            }
    }
}