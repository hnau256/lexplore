package hnau.lexplore.ui.model.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import hnau.lexplore.common.ui.uikit.ScreenContent
import hnau.lexplore.common.ui.uikit.ScreenContentDependencies
import hnau.lexplore.exercise.dto.DictionaryWord
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.forgettingFactor
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class EditProjector(
    scope: CoroutineScope,
    private val model: EditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun screenContent(): ScreenContentDependencies

        fun word(): EditWordProjector.Dependencies
    }

    private val editWordToLearnProjector: StateFlow<Pair<WordToLearn, EditWordProjector>?> = model
        .editWordToLearnModel
        .mapWithScope(scope) { editWordScope, wordWithModelOrNull ->
            wordWithModelOrNull?.let { (wordToLearn, model) ->
                val projector = EditWordProjector(
                    scope = editWordScope,
                    model = model,
                    dependencies = dependencies.word(),
                )
                wordToLearn to projector
            }
        }

    @Composable
    fun Content() {
        ScreenContent(
            dependencies = remember(dependencies) { dependencies.screenContent() },
            topAppBarContent = {
                Title(
                    text = model.name.name,
                )
            }
        ) { contentPadding ->
            val edit: Pair<WordToLearn, EditWordProjector>? by editWordToLearnProjector.collectAsState()
            LazyColumn(
                state = model.scrollState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = model.words,
                    key = { it.toLearn.word }
                ) { word ->
                    WordContent(
                        word = word,
                        edit = edit,
                    )
                }
            }
        }
    }

    @Composable
    private fun WordContent(
        word: DictionaryWord,
        edit: Pair<WordToLearn, EditWordProjector>?,
    ) {
        val toLearn = word.toLearn
        val wordInfoOrNull by model
            .getWordInfo(toLearn)
            .collectAsState()
        val forgettingFactor = wordInfoOrNull
            .forgettingFactor
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
                    modifier = Modifier
                        .clickable { model.updateSelectedWord(toLearn) },
                    headlineContent = { Text(toLearn.word) },
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

                else -> localEditModel.Content()
            }
        }
    }
}