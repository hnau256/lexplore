package hnau.lexplore.ui.model.edit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.common.ui.utils.TextFieldValueSerializer
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.Fsrs
import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.Stability
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class EditWordModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val wordToLearn: WordToLearn,
    private val onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val input: MutableStateFlow<@Serializable(TextFieldValueSerializer::class) TextFieldValue>,
    ) {

        constructor(
            initialValue: Stability,
        ) : this(
            input = MutableStateFlow(
                String.format(Locale.US, "%.4f", initialValue.days).let { stabilityString ->
                    TextFieldValue(
                        text = stabilityString,
                        selection = TextRange(0, stabilityString.length),
                    )
                }
            )
        )
    }

    @Pipe
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository
    }

    val input: MutableStateFlow<TextFieldValue>
        get() = skeleton.input

    private val savingInProgressRegistry = InProgressRegistry()

    fun save(): Boolean {
        val newStability = skeleton
            .input
            .value
            .text
            .replace(',', '.')
            .toDoubleOrNull()
            ?.let { days ->
                Stability(days.coerceAtLeast(Fsrs.minStability.days).days)
            }
            ?: return false
        scope.launch {
            savingInProgressRegistry.executeRegistered {
                val now = Clock.System.now()
                val newInfo = dependencies
                    .knowledgeRepository[wordToLearn]
                    .value
                    .foldNullable(
                        ifNull = {
                            WordInfo(
                                stability = newStability,
                                difficulty = Difficulty(5.0),
                                lastAnswerTimestamp = now,
                            )
                        },
                        ifNotNull = { info ->
                            info.copy(
                                stability = newStability,
                                lastAnswerTimestamp = now,
                            )
                        },
                    )
                dependencies.knowledgeRepository.update(
                    key = wordToLearn,
                    info = newInfo,
                )
            }
            onReady()
        }
        return true
    }
}
