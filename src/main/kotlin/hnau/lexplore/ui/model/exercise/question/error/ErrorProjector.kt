package hnau.lexplore.ui.model.exercise.question.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.TextInput
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.progressindicator.chipInProgressLeadingContent
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.table.cellBox
import hnau.lexplore.common.ui.uikit.table.subtable
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class ErrorProjector(
    private val scope: CoroutineScope,
    private val model: ErrorModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation)
        ) {
            Table(
                orientation = TableOrientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                cellBox(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.separation),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.error,
                        ) { Clear }
                        Text(
                            text = model.incorrectInput,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                cell { corners ->
                    Chip(
                        style = ChipStyle.button,
                        onClick = { model.onTypo(model.selectedSureness) },
                        content = { Text(text = stringResource(R.string.question_typo)) },
                        shape = corners.toShape(),
                    )
                }
            }
            Table(
                orientation = TableOrientation.Vertical,
                modifier = Modifier.fillMaxWidth(),
            ) {
                subtable {
                    cellBox(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.separation),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                            ) { Done }
                            Text(
                                text = model.wordToLearn.word,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    cell { corners ->
                        val speakOrNull by model.correctWorkSpeaker.collectAsState()
                        Chip(
                            style = ChipStyle.button,
                            leading = chipInProgressLeadingContent(
                                inProgress = speakOrNull == null,
                            ),
                            onClick = speakOrNull,
                            content = { Icon { RecordVoiceOver } },
                            shape = corners.toShape(),
                        )
                    }
                }
                cell { corners ->
                    val focusRequester = remember { FocusRequester() }
                    TextInput(
                        value = model.input,
                        shape = corners.toShape(),
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
        }
    }
}