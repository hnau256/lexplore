package hnau.lexplore.common.ui.uikit.chip

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ChipSize(
    val height: Dp,
    val getTextStyle: @Composable () -> TextStyle,
    val contentEdgeSeparation: Dp,
    val contentSideContentSeparation: Dp,
    val sideContentEdgeSeparation: Dp,
    val sideContentSize: Dp,
) {

    companion object {

        val small = ChipSize(
            height = 32.dp,
            getTextStyle = { MaterialTheme.typography.bodyMedium },
            contentEdgeSeparation = 12.dp,
            contentSideContentSeparation = 2.dp,
            sideContentEdgeSeparation = 6.dp,
            sideContentSize = 20.dp
        )

        val medium = ChipSize(
            height = 40.dp,
            getTextStyle = { MaterialTheme.typography.bodyMedium },
            contentEdgeSeparation = 12.dp,
            contentSideContentSeparation = 4.dp,
            sideContentEdgeSeparation = 8.dp,
            sideContentSize = 24.dp
        )

        val large = ChipSize(
            height = 48.dp,
            getTextStyle = { MaterialTheme.typography.bodyLarge },
            contentEdgeSeparation = 16.dp,
            contentSideContentSeparation = 6.dp,
            sideContentEdgeSeparation = 10.dp,
            sideContentSize = 26.dp
        )

        val default: ChipSize
            get() = medium
    }
}