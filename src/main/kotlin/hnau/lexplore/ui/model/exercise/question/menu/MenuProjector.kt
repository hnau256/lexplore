package hnau.lexplore.ui.model.exercise.question.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import hnau.lexplore.R
import hnau.lexplore.common.ui.uikit.chip.Chip
import hnau.lexplore.common.ui.uikit.chip.ChipStyle
import hnau.lexplore.common.ui.uikit.row.ChipsRow
import hnau.lexplore.common.ui.uikit.utils.Dimens
import hnau.lexplore.common.ui.utils.collectAsMutableState
import hnau.lexplore.common.ui.utils.horizontalDisplayPadding
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
            modifier = Modifier.horizontalDisplayPadding(),
        ) {
            Text(
                text = stringResource(R.string.sureness_title),
                style = MaterialTheme.typography.titleMedium,
            )
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
        }
    }
}