package hnau.lexplore.common.ui.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<Loadable<T>>.LoadableContent(
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<Loadable<T>>.() -> ContentTransform,
    loadingContent: @Composable () -> Unit = { ProgressIndicatorPanel() },
    readyContent: @Composable (value: T) -> Unit,
) {
    StateContent(
        modifier = modifier,
        contentKey = { localValue ->
            localValue.fold(
                ifLoading = { false },
                ifReady = { true },
            )
        },
        label = "LoadingOrReady",
        transitionSpec = transitionSpec,
    ) { localValue ->
        localValue.fold(
            ifLoading = { loadingContent() },
            ifReady = { value -> readyContent(value) }
        )
    }
}