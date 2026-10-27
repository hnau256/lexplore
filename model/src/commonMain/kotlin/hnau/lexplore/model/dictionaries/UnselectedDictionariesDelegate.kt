package hnau.lexplore.model.dictionaries

import arrow.core.getOrElse
import hnau.lexplore.common.dictionary.DictionaryName
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.settings.Setting
import hnau.lexplore.common.settings.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.stringSplit
import org.hnau.commons.kotlin.mapper.toListMapper

class UnselectedDictionariesDelegate(
    scope: CoroutineScope,
    settings: AppSettings,
    @PublishedApi
    internal val overwritten: MutableStateFlow<Set<DictionaryName>?>,
) {

    private val setting: Setting<Set<DictionaryName>> = settings["unselected_dictionaries"]
        .map(
            Mapper.stringSplit('|') +
                Mapper(::DictionaryName, DictionaryName::name).toListMapper() +
                Mapper(List<DictionaryName>::toSet, Set<DictionaryName>::toList),
        )

    private val persisted: StateFlow<Set<DictionaryName>> = setting
        .state
        .mapState(scope) { valueOrNone ->
            valueOrNone.getOrElse { emptySet() }
        }

    val unselectedNames: StateFlow<Set<DictionaryName>> = overwritten
        .flatMapState(
            scope = scope,
        ) { overwrittenValue ->
            overwrittenValue
                ?.let(::MutableStateFlow)
                ?: persisted
        }

    fun update(
        newValue: (Set<DictionaryName>) -> Set<DictionaryName>,
    ) {
        overwritten.value = newValue(unselectedNames.value)
    }

    suspend fun apply() {
        setting.update(unselectedNames.value)
    }
}
