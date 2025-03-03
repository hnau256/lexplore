package hnau.lexplore.common.ui.uikit.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.ui.uikit.progressindicator.ProgressIndicatorPanel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<Loadable<T>>.LoadableContent(
    modifier: Modifier = Modifier,
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
    ) { localValue ->
        localValue.fold(
            ifLoading = { ProgressIndicatorPanel() },
            ifReady = { value -> readyContent(value) }
        )
    }
}