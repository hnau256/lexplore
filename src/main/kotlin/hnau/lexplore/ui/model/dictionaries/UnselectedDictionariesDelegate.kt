package hnau.lexplore.ui.model.dictionaries

import arrow.core.getOrElse
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapStateLite
import hnau.lexplore.common.kotlin.mapper.Mapper
import hnau.lexplore.common.kotlin.mapper.plus
import hnau.lexplore.common.kotlin.mapper.stringSplit
import hnau.lexplore.common.kotlin.mapper.toListMapper
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.data.settings.Setting
import hnau.lexplore.data.settings.map
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UnselectedDictionariesDelegate(
    scope: CoroutineScope,
    settings: AppSettings,
    @PublishedApi
    internal val overwritten: MutableStateFlow<Set<DictionaryName>?>,
) {

    private val setting: Setting<Set<DictionaryName>> =
        settings["unselected_dictionaries"]
            .map(
                Mapper.stringSplit('|') +
                Mapper(::DictionaryName, DictionaryName::name).toListMapper() +
                        Mapper(List<DictionaryName>::toSet, Set<DictionaryName>::toList),
            )

    val unselectedNames: StateFlow<Set<DictionaryName>> = overwritten
        .flatMapState(
            scope = scope,
        ) { overwritten ->
            overwritten
                ?.let { MutableStateFlow(overwritten) }
                ?: setting.state.mapStateLite { it.getOrElse { emptySet() } }
        }

    inline fun update(
        newValue: (Set<DictionaryName>) -> Set<DictionaryName>,
    ) {
        overwritten.value = newValue(unselectedNames.value)
    }

    suspend fun apply() {
        setting.update(unselectedNames.value)
    }
}