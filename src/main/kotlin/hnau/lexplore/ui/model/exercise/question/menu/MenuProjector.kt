package hnau.lexplore.ui.model.exercise.question.menu

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.table.Table
import hnau.lexplore.common.ui.uikit.table.TableOrientation
import hnau.lexplore.common.ui.uikit.table.subtable
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
import hnau.lexplore.exercise.dto.Answer
import org.hnau.commons.gen.pipe.annotations.Pipe
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