package hnau.lexplore.ui.model.edit

import androidx.compose.foundation.lazy.LazyListState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.LazyListStateSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.DictionaryWord
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.lexplore.exercise.dto.forgettingFactor
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class EditModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val name: DictionaryName,
        @Serializable(LazyListStateSerializer::class)
        val scrollState: LazyListState = LazyListState(),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedWordToLearn: MutableStateFlow<Pair<WordToLearn, EditWordModel.Skeleton>?> =
            MutableStateFlow(null),
    )

    val name: DictionaryName
        get() = skeleton.name

    val scrollState: LazyListState
        get() = skeleton.scrollState

    @Shuffle
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        fun word(): EditWordModel.Dependencies
    }

    fun getWordInfo(
        wordToLearn: WordToLearn,
    ): StateFlow<WordInfo?> = dependencies
        .knowledgeRepository
        .get(wordToLearn)

    fun updateSelectedWord(
        wordToLearn: WordToLearn,
    ) {
        val forgettingFactor = getWordInfo(wordToLearn).value.forgettingFactor
        skeleton.selectedWordToLearn.value = wordToLearn to EditWordModel.Skeleton(
            initialValue = forgettingFactor,
        )
    }

    val words: List<DictionaryWord> = dependencies
        .dictionaries[skeleton.name]
        .dictionaryWords

    val editWordToLearnModel: StateFlow<Pair<WordToLearn, EditWordModel>?> = skeleton
        .selectedWordToLearn
        .mapWithScope(scope) { selectionScope, wordWithSkeleton ->
            val (wordToLearn, editWordSkeleton) = wordWithSkeleton ?: return@mapWithScope null
            val model = EditWordModel(
                scope = selectionScope,
                wordToLearn = wordToLearn,
                skeleton = editWordSkeleton,
                dependencies = dependencies.word(),
                onReady = { skeleton.selectedWordToLearn.value = null },
            )
            wordToLearn to model
        }
}