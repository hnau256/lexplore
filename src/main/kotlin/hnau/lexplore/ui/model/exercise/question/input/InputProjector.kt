package hnau.lexplore.ui.model.exercise.question.input

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import hnau.lexplore.common.ui.uikit.chip.ChipStyle.Companion.builder
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
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
        Table(
            orientation = TableOrientation.Horizontal,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
        ) {
            cell { corners ->
                val focusRequester = remember { FocusRequester() }
                TextInput(
                    shape = corners.toShape(),
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
            }
            cell { corners ->
                val done = model.done.collectAsState().value
                Chip(
                    modifier = Modifier
                        .fillMaxHeight(),
                    content = { Icon { Done } },
                    size = ChipSize.large,
                    shape = corners.toShape(),
                    style = when (done) {
                        null -> ChipStyle.chip
                        else -> ChipStyle.button
                    },
                    onClick = done,
                )
            }
        }
    }
}