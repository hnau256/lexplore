package hnau.lexplore.common.ui.uikit.bubble

import kotlinx.coroutines.flow.StateFlow

interface BubblesProvider {

    val visibleBubble: StateFlow<Bubble?>
}
