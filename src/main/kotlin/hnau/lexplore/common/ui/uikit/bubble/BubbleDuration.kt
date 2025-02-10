package hnau.lexplore.common.ui.uikit.bubble

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@JvmInline
value class BubbleDuration(
    val duration: Duration,
) {

    companion object {

        val default = BubbleDuration(
            duration = 3.seconds,
        )
    }
}
