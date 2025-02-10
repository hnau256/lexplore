package hnau.lexplore.ui.model.dictionaries

import arrow.core.getOrElse
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapStateLite
import hnau.lexplore.common.kotlin.mapper.Mapper
import hnau.lexplore.common.kotlin.mapper.plus
import hnau.lexplore.common.kotlin.mapper.stringSplit
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.data.settings.Setting
import hnau.lexplore.data.settings.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UnselectedDictionariesDelegate(
    scope: CoroutineScope,
    settings: AppSettings,
    private val overwritten: MutableStateFlow<Set<String>?>,
) {

    private val setting: Setting<Set<String>> =
        settings["unselected_dictionaries"]
            .map(
                Mapper.stringSplit('|') +
                        Mapper(List<String>::toSet, Set<String>::toList),
            )

    val unselectedNames: StateFlow<Set<String>> =
        overwritten.flatMapState(
            scope = scope,
        ) { overwritten ->
            overwritten
                ?.let { MutableStateFlow(overwritten) }
                ?: setting.state.mapStateLite { it.getOrElse { emptySet() } }
        }

    fun update(
        newValue: (Set<String>) -> Set<String>,
    ) {
        overwritten.value = newValue(unselectedNames.value)
    }

    suspend fun apply() {
        setting.update(unselectedNames.value)
    }
}