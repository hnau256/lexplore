package hnau.lexplore.ui.model.exercise.question.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.Icon
import hnau.lexplore.common.ui.utils.collectAsMutableState
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.Sureness
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope

class MenuProjector(
    private val scope: CoroutineScope,
    private val model: MenuModel,
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .padding(Dimens.separation)
                .fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.sureness_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Separator(Dimens.smallSeparation)
            var selectedSureness by model.selectedSureness.collectAsMutableState()
            ChipsRow(
                all = Sureness.entries,
            ) { sureness, shape ->
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
                    shape = shape,
                    style = when (sureness) {
                        selectedSureness -> ChipStyle.button
                        else -> ChipStyle.chip
                    }
                )
            }
            Separator()
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.chipsSeparation),
            ) {
                Chip(
                    leading = { Icon { School } },
                    content = { Text(stringResource(R.string.question_menu_mark_as_known)) },
                    shape = HnauShape.inRow.start,
                    style = ChipStyle.button,
                    onClick = { model.onAnswer(Answer.AlmostKnown) },
                )
                Chip(
                    leading = { Icon { Delete } },
                    content = { Text(stringResource(R.string.question_menu_exclude)) },
                    shape = HnauShape.inRow.end,
                    style = ChipStyle.button,
                    onClick = { model.onAnswer(Answer.Useless) },
                )
            }
        }
    }
}