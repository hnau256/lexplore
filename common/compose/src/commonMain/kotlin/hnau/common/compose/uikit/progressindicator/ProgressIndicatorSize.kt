package hnau.common.compose.uikit.progressindicator

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ProgressIndicatorSize(
    val diameter: Dp,
    val strokeWidth: Dp,
) {

    companion object {

        val small = ProgressIndicatorSize(
            diameter = 24.dp,
            strokeWidth = 3.dp,
        )

        val medium = ProgressIndicatorSize(
            diameter = 40.dp,
            strokeWidth = 4.dp,
        )

        val large = ProgressIndicatorSize(
            diameter = 56.dp,
            strokeWidth = 5.dp,
        )

        val default: ProgressIndicatorSize
            get() = medium
    }
}