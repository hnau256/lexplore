package hnau.lexplore.ui.model.exercise.question.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.exercise.dto.FsrsRating
import hnau.lexplore.ui.model.exercise.question.labelRes
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.ifTrue
import kotlinx.coroutines.CoroutineScope

class InputProjector(
    private val scope: CoroutineScope,
    private val model: InputModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        val focusRequester = remember { FocusRequester() }
        val hasInput by model.hasInput.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            TextInput(
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None,
                ),
                keyboardActions = KeyboardActions {},
                maxLines = 1,
                value = model.input,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            Table(
                orientation = TableOrientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                FsrsRating.entries.forEach { rating ->
                    cell { corners ->
                        val enabled = rating == FsrsRating.Again || hasInput
                        Chip(
                            modifier = Modifier.weight(1f),
                            content = {
                                Text(
                                    text = stringResource(rating.labelRes),
                                    maxLines = 1,
                                )
                            },
                            shape = corners.toShape(),
                            style = enabled.foldBoolean(
                                ifTrue = { ChipStyle.button },
                                ifFalse = { ChipStyle.chip },
                            ),
                            onClick = enabled.ifTrue {
                                { model.onGrade(rating) }
                            },
                        )
                    }
                }
            }
        }
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }
}
