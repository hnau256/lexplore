package hnau.lexplore.common.ui.projector.stack

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.lexplore.common.ui.utils.getTransitionSpecForHorizontalSlide
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

@Composable
fun <P> StateFlow<StackProjectorTail<P>>.Content(
    content: @Composable (projector: P) -> Unit,
) {
    val currentTail: StackProjectorTail<P> by collectAsState()
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = currentTail,
        transitionSpec = getTransitionSpecForHorizontalSlide(
            duration = AnimationDuration,
        ) {
            when (targetState.isNew) {
                true -> AnimationSlideFactor
                false -> -AnimationSlideFactor
                null -> 0
            }
        },
    ) { (projector, _) ->
        content(projector)
    }
}

private const val AnimationSlideFactor = 0.5
private val AnimationDuration = 0.3.seconds
