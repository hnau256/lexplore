package hnau.common.compose.projector

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.compose.utils.getTransitionSpecForHorizontalSlide
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

@Composable
fun <P> StateFlow<StackProjectorTail<P>>.Content(
    content: @Composable P.() -> Unit,
) {
    val currentTail by collectAsState()
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
        projector.content()
    }
}

private const val AnimationSlideFactor = 0.5
private val AnimationDuration = 0.3.seconds
