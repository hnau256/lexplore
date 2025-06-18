package hnau.lexplore.ui.model.exercise.question.menu

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.Separator
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.row.ChipsRow
import hnau.lexplore.common.ui.uikit.shape.HnauShape
import hnau.lexplore.common.ui.uikit.shape.end
import hnau.lexplore.common.ui.uikit.shape.inRow
import hnau.lexplore.common.ui.uikit.shape.start
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.table.cellBox
import hnau.lexplore.common.ui.uikit.table.subtable
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.collectAsMutableState
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.Sureness
import hnau.pipe.annotations.Pipe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope

class MenuProjector(
    private val scope: CoroutineScope,
    private val model: MenuModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        var selectedSureness by model.selectedSureness.collectAsMutableState()
        val context = LocalContext.current
        val actions = remember(context) {
            Action.buildList(
                context = context,
            )
        }
        Table(
            orientation = TableOrientation.Vertical,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding()
        ) {
            subtable {
                cellBox(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = Dimens.separation,
                        ),
                        text = stringResource(R.string.sureness_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Sureness
                    .entries
                    .forEach { sureness ->
                        cell { corners ->
                            Chip(
                                onClick = { selectedSureness = sureness },
                                content = {
                                    Text(
                                        text = stringResource(
                                            when (sureness) {
                                                Sureness.Low -> R.string.sureness_low
                                                Sureness.Medium -> R.string.sureness_medium
                                                Sureness.Height -> R.string.sureness_high
                                            }
                                        ),
                                    )
                                },
                                shape = corners.toShape(),
                                style = when (sureness) {
                                    selectedSureness -> ChipStyle.button
                                    else -> ChipStyle.chip
                                }
                            )
                        }
                    }
            }
            subtable {
                actions.forEach { action ->
                    cell { corners ->
                        Chip(
                            modifier = Modifier.weight(1f),
                            leading = { Icon(chooseIcon = action.icon) },
                            content = { Text(action.title) },
                            shape = corners.toShape(),
                            style = ChipStyle.button,
                            onClick = { action.onClick(model) },
                        )
                    }
                }
            }
        }
    }

    private data class Action(
        val icon: Icons.Filled.() -> ImageVector,
        val title: String,
        val onClick: MenuModel.() -> Unit,
    ) {

        companion object {

            fun buildList(
                context: Context,
            ): ImmutableList<Action> = persistentListOf(
                Action(
                    title = context.getString(R.string.question_menu_mark_as_known),
                    icon = { School },
                    onClick = { onAnswer(Answer.AlmostKnown) },
                ),
                Action(
                    title = context.getString(R.string.question_menu_exclude),
                    icon = { Delete },
                    onClick = { onAnswer(Answer.Useless) },
                ),
            )
        }
    }
}