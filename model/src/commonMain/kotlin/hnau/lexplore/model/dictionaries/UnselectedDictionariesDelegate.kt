package hnau.lexplore.model.dictionaries

import arrow.core.getOrElse
import hnau.lexplore.common.dictionary.DictionaryName
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.settings.Setting
import hnau.lexplore.common.settings.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.stringSplit
import org.hnau.commons.kotlin.mapper.toListMapper

class UnselectedDictionariesDelegate(
    scope: CoroutineScope,
    settings: AppSettings,
    private val validNames: Set<DictionaryName>,
    @PublishedApi
    internal val overwritten: MutableStateFlow<Set<DictionaryName>?>,
) {

    private val setting: Setting<Set<DictionaryName>> = settings["unselected_dictionaries"]
        .map(
            scope = scope,
            mapper = Mapper.stringSplit('|') +
                    Mapper(::DictionaryName, DictionaryName::name).toListMapper() +
                    Mapper(List<DictionaryName>::toSet, Set<DictionaryName>::toList),
        )

    private val persisted: StateFlow<Set<DictionaryName>> = setting
        .state
        .mapState(scope) { valueOrNone ->
            valueOrNone.getOrElse { emptySet() }
        }

    val unselectedNames: StateFlow<Set<DictionaryName>> = derivedStateFlowOf(scope) {
        overwritten.state
            .foldNullable(
                ifNull = { persisted.state },
                ifNotNull = { it },
            )
            .filter(validNames::contains)
            .toSet()
    }

    fun update(
        newValue: Set<DictionaryName>,
    ) {
        overwritten.value = newValue
    }

    suspend fun apply(
        unselectedNames: Set<DictionaryName>,
    ) {
        setting.update(unselectedNames)
    }
}
