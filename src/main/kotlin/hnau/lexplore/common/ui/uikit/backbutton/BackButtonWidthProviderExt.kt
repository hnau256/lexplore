package hnau.lexplore.common.ui.uikit.backbutton

import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import hnau.lexplore.common.ui.uikit.utils.appInsets
import hnau.lexplore.common.ui.uikit.Separator as UiKitSpace

@Composable
fun BackButtonWidthProvider.Space() {
    val width by backButtonWidth
    val startPadding = appInsets.calculateStartPadding(LocalLayoutDirection.current)
    UiKitSpace(size = width + startPadding)
}