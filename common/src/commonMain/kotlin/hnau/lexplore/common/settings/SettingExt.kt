package hnau.lexplore.common.settings

import arrow.core.Option
import org.hnau.commons.kotlin.mapper.Mapper

val <T> Setting<T>.value: Option<T>
    get() = state.value

fun <I, O> Setting<I>.map(
    mapper: Mapper<I, O>,
): Setting<O> = SettingImpl(
    initialValue = value.map(mapper.direct),
    update = { newValue ->
        val transformedNewValue = mapper.reverse(newValue)
        update(transformedNewValue)
    },
)
