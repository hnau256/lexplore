package hnau.lexplore.projector.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import hnau.common.compose.uikit.utils.Dimens
import hnau.lexplore.projector.common.backbutton.BackButtonWidthProvider
import hnau.lexplore.projector.common.backbutton.Space
import hnau.shuffler.annotations.Shuffle

class TopAppBarDelegate(
    private val dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val backButtonWidthProvider: BackButtonWidthProvider
    }

    @Composable
    fun Content(
        content: @Composable RowScope.(
            startContentPadding: Dp,
        ) -> Unit,
    ) = Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.rowHeight)
            .background(MaterialTheme.colors.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dependencies.backButtonWidthProvider.Space()
        val buttonWidth by dependencies.backButtonWidthProvider.backButtonWidth
        val startContentPadding = remember(buttonWidth) {
            lerp(
                start = Dimens.separation,
                stop = 0.dp,
                fraction = buttonWidth / BackButtonWidthProvider.maxBackButtonSize,
            )
        }
        content(startContentPadding)
    }
}