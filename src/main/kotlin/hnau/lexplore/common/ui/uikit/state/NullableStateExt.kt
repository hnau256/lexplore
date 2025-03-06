package hnau.lexplore.common.ui.uikit.state

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T?>.NullableStateContent(
    modifier: Modifier = Modifier,
    nullContent: @Composable () -> Unit = {},
    transitionSpec: AnimatedContentTransitionScope<T?>.() -> ContentTransform,
    anyContent: @Composable (value: T & Any) -> Unit,
) {
    StateContent(
        modifier = modifier,
        label = "Option",
        contentKey = { localValue -> localValue != null },
        transitionSpec = transitionSpec,
    ) { localValue ->
        when (localValue) {
            null -> nullContent()
            else -> anyContent(localValue)
        }
    }
}