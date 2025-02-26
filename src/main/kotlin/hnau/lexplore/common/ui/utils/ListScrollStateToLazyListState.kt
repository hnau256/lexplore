/*
package hnau.lexplore.common.ui.hnau.lexplore.common.ui.color.material.utils.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import hnau.lexplore.common.model.ListScrollState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MutableStateFlow<ListScrollState>.toLazyListState(): LazyListState {
    val state = remember {
        val initial = value
        LazyListState(
            firstVisibleItemIndex = initial.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = initial.firstVisibleItemScrollOffset,
        )
    }
    LaunchedEffect(
        state.firstVisibleItemIndex,
        state.firstVisibleItemScrollOffset,
    ) {
        value = ListScrollState(
            firstVisibleItemIndex = state.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset,
        )
    }
    return state
}
*/
