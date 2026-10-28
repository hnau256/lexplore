package hnau.lexplore.common.settings

import arrow.core.Option
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.mapper.Mapper

fun <I, O> Setting<I>.map(
    scope: CoroutineScope,
    mapper: Mapper<I, O>,
): Setting<O> = object : Setting<O> {

    override val state: StateFlow<Option<O>> =
        this@map.state.mapState(scope) { value ->
            value.map(mapper.direct)
        }

    override suspend fun update(
        newValue: O,
    ) {
        this@map.update(mapper.reverse(newValue))
    }
}
