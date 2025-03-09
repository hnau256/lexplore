package hnau.lexplore.ui.model.dictionaries

import androidx.compose.foundation.lazy.LazyListState
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.LazyListStateSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.exercise.LearningConstants
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.lexplore.exercise.dto.forgettingFactor
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class DictionariesModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val openQuestions: (Set<DictionaryName>) -> Unit,
    val edit: (DictionaryName) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(LazyListStateSerializer::class)
        val scrollState: LazyListState = LazyListState(),

        @Serializable(MutableStateFlowSerializer::class)
        val overwrittenUnselectedDictionaries: MutableStateFlow<Set<DictionaryName>?> =
            MutableStateFlow(null),
    )

    @Shuffle
    interface Dependencies {

        val appSettings: AppSettings

        val dictionaries: Dictionaries

        val knowledgeRepository: KnowledgeRepository
    }

    val scrollState: LazyListState
        get() = skeleton.scrollState

    @PublishedApi
    internal val unselectedDictionariesDelegate = UnselectedDictionariesDelegate(
        scope = scope,
        settings = dependencies.appSettings,
        overwritten = skeleton.overwrittenUnselectedDictionaries,
    )

    val unselectedNames: StateFlow<Set<DictionaryName>>
        get() = unselectedDictionariesDelegate.unselectedNames

    inline fun update(
        newValue: (Set<DictionaryName>) -> Set<DictionaryName>,
    ) {
        unselectedDictionariesDelegate.update(newValue)
    }

    private val loadingWordsInProgressRegistry = InProgressRegistry()

    val isLoadingWords: StateFlow<Boolean>
        get() = loadingWordsInProgressRegistry.isProgress

    val start: StateFlow<(() -> Unit)?> = unselectedDictionariesDelegate
        .unselectedNames
        .mapState(
            scope = scope,
        ) { unselectedDictionaries ->
            val dictionariesToStart = dependencies
                .dictionaries
                .names
                .filter { name -> name !in unselectedDictionaries }
                .takeIf { it.isNotEmpty() }
                ?: return@mapState null
            {
                scope.launch {
                    loadingWordsInProgressRegistry.executeRegistered {
                        unselectedDictionariesDelegate.apply()
                        openQuestions(dictionariesToStart.toSet())
                    }
                }
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
                    .forgettingFactor > LearningConstants.initialForgettingFactor
            }
        )
    }

    val allDictionaryNamesSet: Set<DictionaryName> =
        dictionaryNames.toSet()
}