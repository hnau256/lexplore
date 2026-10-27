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
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class DictionariesModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val openQuestions: (Set<DictionaryName>) -> Unit,
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

    @PublishedApi
    internal val unselectedDictionariesDelegate = UnselectedDictionariesDelegate(
        scope = scope,
        settings = dependencies.appSettings,
        overwritten = skeleton.overwrittenUnselectedDictionaries,
    )

    val unselectedNames: StateFlow<Set<DictionaryName>>
        get() = unselectedDictionariesDelegate.unselectedNames

    fun update(
        newValue: (Set<DictionaryName>) -> Set<DictionaryName>,
    ) {
        unselectedDictionariesDelegate.update(newValue)
    }

    val start: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(scope) {
            unselectedDictionariesDelegate.apply()
            val selectedNames = allDictionaryNamesSet - unselectedNames.value
            selectedNames.isNotEmpty().ifTrue {
                openQuestions(selectedNames)
            }
        }

    data class Item(
        val dictionaryName: DictionaryName,
        val totalWordsCount: Int,
        val knownWordsCount: Int,
    )

    private val dictionaryNames: List<DictionaryName> = dependencies
        .dictionaries
        .names

    val items: List<Item> = dictionaryNames.map { dictionaryName ->
        val words = dependencies
            .dictionaries[dictionaryName]
            .dictionaryWords
        Item(
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
            }
        )
    }

    val allDictionaryNamesSet: Set<DictionaryName> =
        dictionaryNames.toSet()

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}

private fun WordInfo.isKnown(): Boolean = stability > Fsrs.minStability
