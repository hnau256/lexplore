package hnau.lexplore.projector.common.backbutton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import hnau.common.compose.uikit.Space as UiKitSpace

@Composable
fun BackButtonWidthProvider.Space() {
    val width by backButtonWidth
    UiKitSpace(size = width)
}