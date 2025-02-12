package hnau.lexplore.ui.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.common.ui.utils.LazyListStateSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.LearningConstants
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordToLearn
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
        val dictionaryName: String,
        val words: List<Word>,
        @Serializable(LazyListStateSerializer::class)
        val scrollState: LazyListState = LazyListState(),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedWord: MutableStateFlow<Pair<WordToLearn, EditWordModel.Skeleton>?> =
            MutableStateFlow(null),
    )

    private val editWordModel: StateFlow<Pair<WordToLearn, EditWordModel>?> = run {
        val wordsByToLearn: Map<WordToLearn, Word> = skeleton
            .words
            .associateBy(Word::toLearn)

        skeleton
            .selectedWord
            .mapWithScope(scope) { selectionScope, wordWithSkeleton ->
                val (wordToLearn, editWordSkeleton) = wordWithSkeleton ?: return@mapWithScope null
                val word = wordsByToLearn.getValue(wordToLearn)
                val model = EditWordModel(
                    scope = selectionScope,
                    word = word,
                    skeleton = editWordSkeleton,
                    dependencies = dependencies.word(),
                    onReady = { skeleton.selectedWord.value = null },
                )
                wordToLearn to model
            }
    }

    @Shuffle
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository

        fun word(): EditWordModel.Dependencies
    }

    @Shuffle
    interface ContentDependencies {

        fun screenContent(): ScreenContentDependencies

        fun word(): EditWordModel.ContentDependencies
    }

    @Composable
    fun Content(
        dependencies: ContentDependencies,
    ) {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title(
                    text = skeleton.dictionaryName,
                )
            }
        ) { contentPadding ->
            val edit: Pair<WordToLearn, EditWordModel>? by editWordModel.collectAsState()
            LazyColumn(
                state = skeleton.scrollState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    skeleton.words,
                ) { word ->
                    WordContent(
                        word = word,
                        edit = edit,
                        dependencies = dependencies,
                    )
                }
            }
        }
    }

    @Composable
    private fun WordContent(
        word: Word,
        edit: Pair<WordToLearn, EditWordModel>?,
        dependencies: ContentDependencies,
    ) {
        val dataRepository = this@EditModel
            .dependencies
            .knowledgeRepository
        val toLearn = word.toLearn
        val wordInfoOrNull by dataRepository
            .get(toLearn)
            .collectAsState()
        val forgettingFactor = wordInfoOrNull
            ?.forgettingFactor
            ?: LearningConstants.initialForgettingFactor
        AnimatedContent(
            targetState = edit
                ?.takeIf { it.first == toLearn }
                ?.second,
            contentKey = { it != null },
            contentAlignment = Alignment.Center,
            label = "ViewOrEdit"
        ) { localEditModel ->
            when (localEditModel) {
                null -> ListItem(
                    modifier = Modifier.clickable {
                        val wordSkeleton = EditWordModel.Skeleton(
                            initialValue = forgettingFactor,
                        )
                        skeleton.selectedWord.value = toLearn to wordSkeleton
                    },
                    headlineContent = { Text(word.toLearn.word) },
                    supportingContent = { Text(word.translation.translation) },
                    trailingContent = {
                        Text(
                            text = forgettingFactor
                                .factor
                                .toInt()
                                .toString(),
                        )
                    }
                )

                else -> localEditModel.Content(
                    dependencies = remember(dependencies) { dependencies.word() },
                )
            }
        }
    }
}