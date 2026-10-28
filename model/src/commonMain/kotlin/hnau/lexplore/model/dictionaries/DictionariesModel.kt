package hnau.lexplore.model.dictionaries

import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.dictionary.DictionaryName
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.fsrs.dto.WordInfo
import hnau.lexplore.fsrs.Fsrs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import arrow.core.NonEmptySet
import arrow.core.toNonEmptySetOrNull
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState

class DictionariesModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val openQuestions: (NonEmptySet<DictionaryName>) -> Unit,
) {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val overwrittenUnselectedDictionaries: MutableStateFlow<Set<DictionaryName>?> =
            null.toMutableStateFlowAsInitial(),
    )

    @Pipe
    interface Dependencies {

        val appSettings: AppSettings

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries
    }

    private val dictionaryNames: List<DictionaryName> = dependencies
        .dictionaries
        .names

    private val allDictionaryNamesSet: Set<DictionaryName> =
        dictionaryNames.toSet()

    @PublishedApi
    internal val unselectedDictionariesDelegate = UnselectedDictionariesDelegate(
        scope = scope,
        settings = dependencies.appSettings,
        validNames = allDictionaryNamesSet,
        overwritten = skeleton.overwrittenUnselectedDictionaries,
    )

    private val unselectedNames: StateFlow<Set<DictionaryName>>
        get() = unselectedDictionariesDelegate.unselectedNames

    private fun update(
        newValue: Set<DictionaryName>,
    ) {
        unselectedDictionariesDelegate.update(newValue)
    }

    val start: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>?> = unselectedNames
        .flatMapWithScope(scope) { scope, unselected ->
            (allDictionaryNamesSet - unselected)
                .toNonEmptySetOrNull()
                .foldNullable(
                    ifNull = { null.toMutableStateFlowAsInitial() },
                    ifNotNull = { selectedNames ->
                        actionOrCancelIfExecuting(scope) {
                            unselectedDictionariesDelegate.apply(
                                unselectedNames = unselected,
                            )
                            openQuestions(selectedNames)
                        }
                    },
                )
        }

    private data class ItemInfo(
        val dictionaryName: DictionaryName,
        val totalWordsCount: Int,
        val knownWordsCount: Int,
    )

    private val itemInfos: List<ItemInfo> = dictionaryNames.map { dictionaryName ->
        val words = dependencies
            .dictionaries[dictionaryName]
            .dictionaryWords
        ItemInfo(
            dictionaryName = dictionaryName,
            totalWordsCount = words.size,
            knownWordsCount = words.count { word ->
                dependencies
                    .knowledgeRepository[word.toLearn]
                    .value
                    .foldNullable(
                        ifNull = { false },
                        ifNotNull = WordInfo::isKnown,
                    )
            },
        )
    }

    data class Item(
        val dictionaryName: DictionaryName,
        val totalWordsCount: Int,
        val knownWordsCount: Int,
        val isSelected: Boolean,
        val action: ActionOrElse<Unit, CancelOrInProgress.Cancel>?,
    )

    val items: StateFlow<List<Item>> = unselectedNames
        .mapState(scope) { unselected ->
            itemInfos.map { info ->
            val isSelected = info.dictionaryName !in unselected
            val target = isSelected.foldBoolean(
                ifTrue = { unselected + info.dictionaryName },
                ifFalse = { unselected - info.dictionaryName },
            )
                Item(
                    dictionaryName = info.dictionaryName,
                    totalWordsCount = info.totalWordsCount,
                    knownWordsCount = info.knownWordsCount,
                    isSelected = isSelected,
                    action = createAction(
                        current = unselected,
                        target = target,
                    ),
                )
            }
        }

    val selectAll: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>?> = unselectedNames
        .mapState(scope) { unselected ->
            createAction(
                current = unselected,
                target = emptySet(),
            )
        }

    val selectNone: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>?> = unselectedNames
        .mapState(scope) { unselected ->
            createAction(
                current = unselected,
                target = allDictionaryNamesSet,
            )
        }

    private fun createAction(
        current: Set<DictionaryName>,
        target: Set<DictionaryName>,
    ): ActionOrElse<Unit, CancelOrInProgress.Cancel>? = target
        .takeIf { it != current }
        ?.let { target ->
            ActionOrElse.instant {
                update(target)
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}

private fun WordInfo.isKnown(): Boolean = stability > Fsrs.minStability
