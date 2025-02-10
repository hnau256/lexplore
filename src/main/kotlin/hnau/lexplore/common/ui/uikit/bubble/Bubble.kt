package hnau.lexplore.common.ui.uikit.bubble

data class Bubble(
    val text: String,
    val duration: BubbleDuration = BubbleDuration.default,
)
