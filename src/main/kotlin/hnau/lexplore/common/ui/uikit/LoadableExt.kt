package hnau.lexplore.common.ui.uikit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<Loadable<T>>.Content(
    modifier: Modifier = Modifier,
    readyContent: @Composable (value: T) -> Unit,
) {
    val value by collectAsState()
    AnimatedContent(
        targetState = value,
        contentKey = { localValue ->
            localValue.fold(
                ifLoading = { false },
                ifReady = { true },
            )
        },
        modifier = modifier,
        contentAlignment = Alignment.Center,
        label = "LoadingOrReady",
    ) { localValue ->
        localValue.fold(
            ifLoading = { ProgressIndicatorPanel() },
            ifReady = { value -> readyContent(value) }
        )
    }
}