package hnau.lexplore.common.ui.uikit.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arrow.core.Option
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<Option<T>>.OptionStateContent(
    modifier: Modifier = Modifier,
    noneContent: @Composable () -> Unit = {},
    someContent: @Composable (value: T) -> Unit,
) {
    StateContent(
        modifier = modifier,
        label = "Option",
        contentKey = { localValue ->
            localValue.fold(
                ifEmpty = { false },
                ifSome = { true },
            )
        },
    ) { localValue ->
        localValue.fold(
            ifEmpty = { noneContent() },
            ifSome = { value -> someContent(value) }
        )
    }
}