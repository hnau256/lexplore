package hnau.lexplore.ui.model.exercise.question.input

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipSize
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.end
import hnau.lexplore.common.ui.uikit.shape.inRow
import hnau.lexplore.common.ui.uikit.shape.start
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding()
                .height(IntrinsicSize.Max),
        ) {
            val focusRequester = remember { FocusRequester() }
            TextInput(
                shape = HnauShape.inRow.start,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                keyboardActions = KeyboardActions {},
                maxLines = 1,
                value = model.input,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
            )
            LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
            Chip(
                modifier = Modifier
                    .fillMaxHeight(),
                content = { Icon { Done } },
                size = ChipSize.large,
                shape = HnauShape.inRow.end,
                style = ChipStyle.button,
                onClick = { model.onReady(model.input.value.text) },
            )
        }
    }
}