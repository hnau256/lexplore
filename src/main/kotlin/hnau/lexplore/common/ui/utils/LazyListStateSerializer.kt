package hnau.lexplore.common.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import hnau.lexplore.common.kotlin.mapper.Mapper
import hnau.lexplore.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable

object LazyListStateSerializer :
    MappingKSerializer<LazyListStateSerializer.Surrogate, LazyListState>(
        base = Surrogate.serializer(),
        mapper = Mapper(
            direct = { surrogate ->
                LazyListState(
                    firstVisibleItemIndex = surrogate.item,
                    firstVisibleItemScrollOffset = surrogate.offset,
                )
            },
            reverse = { lazyListState ->
                Surrogate(
                    item = lazyListState.firstVisibleItemIndex,
                    offset = lazyListState.firstVisibleItemScrollOffset,
                )
            }
        )
    ) {

    @Serializable
    data class Surrogate(
        val item: Int,
        val offset: Int,
    )
}