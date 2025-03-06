package hnau.lexplore.ui.model.exercise.question

import arrow.core.getOrElse
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.lexplore.common.kotlin.coroutines.mapStateLite
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.getOrInit
import hnau.lexplore.common.kotlin.ifTrue
import hnau.lexplore.common.kotlin.mapper.Mapper
import hnau.lexplore.common.kotlin.mapper.stringToBoolean
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.kotlin.toAccessor
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.data.settings.AppSettings
import hnau.lexplore.data.settings.Setting
import hnau.lexplore.data.settings.map
import hnau.lexplore.exercise.ExerciseWords
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.ui.model.exercise.question.error.ErrorModel
import hnau.lexplore.ui.model.exercise.question.input.InputModel
import hnau.lexplore.ui.model.exercise.question.menu.MenuModel
import hnau.lexplore.utils.TTS
import hnau.lexplore.utils.normalized
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class QuestionModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val onAnswer: suspend (Answer) -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val wordToLearn: WordToLearn,
        @Serializable(MutableStateFlowSerializer::class)
        val previousWord: MutableStateFlow<WordToLearn?>,
        @Serializable(MutableStateFlowSerializer::class)
        val error: MutableStateFlow<ErrorModel.Skeleton?> = MutableStateFlow(null),
        @Serializable(MutableStateFlowSerializer::class)
        val menuIsOpened: MutableStateFlow<Boolean> = MutableStateFlow(false),
        @Serializable(MutableStateFlowSerializer::class)
        val selectedSureness: MutableStateFlow<Sureness> = MutableStateFlow(Sureness.default),
        var input: InputModel.Skeleton? = null,
        var menu: MenuModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        fun input(): InputModel.Dependencies

        fun error(): ErrorModel.Dependencies

        fun menu(): MenuModel.Dependencies

        val exerciseWords: ExerciseWords

        val repository: KnowledgeRepository

        val settings: AppSettings

        val tts: TTS
    }

    val menuIsOpened: MutableStateFlow<Boolean>
        get() = skeleton.menuIsOpened

    private val autoTTSSetting: Setting<Boolean> = dependencies
        .settings["auto_tts"]
        .map(Mapper.stringToBoolean)

    val autoTTS: StateFlow<Boolean>
        get() = autoTTSSetting.state.mapStateLite { it.getOrElse { true } }

    val switchAutoTTS: StateFlow<(() -> Unit)?> = actionOrNullIfExecuting(
        scope = scope,
    ) {
        autoTTSSetting.update(
            newValue = !autoTTS.value,
        )
    }

    data class PreviousWordSpeaker(
        val word: WordToLearn,
        val speak: StateFlow<(() -> Unit)?>,
        val close: () -> Unit,
    )

    val previousWordSpeaker: StateFlow<PreviousWordSpeaker?> = skeleton
        .previousWord
        .mapWithScope(scope) { previousWordScope, previousWordOrNull ->
            previousWordOrNull?.let { previousWord ->
                PreviousWordSpeaker(
                    word = previousWord,
                    speak = actionOrNullIfExecuting(
                        scope = previousWordScope,
                    ) {
                        dependencies.tts.speek(previousWord.word)
                    },
                    close = { skeleton.previousWord.value = null },
                )
            }
        }

    init {
        if (autoTTS.value) {
            previousWordSpeaker
                .value
                ?.speak
                ?.value
                ?.invoke()
        }
    }

    val wordToLearn: WordToLearn
        get() = skeleton.wordToLearn

    val info: WordInfo? = dependencies
        .repository[wordToLearn]
        .value

    private val answeringInProgressRegistry = InProgressRegistry()

    val isAnswering: StateFlow<Boolean>
        get() = answeringInProgressRegistry.isProgress

    val state: StateFlow<QuestionStateModel> = skeleton
        .error
        .mapWithScope(scope) { stateScope, errorSkeleton ->
            when (errorSkeleton) {
                null -> QuestionStateModel.Input(
                    InputModel(
                        scope = stateScope,
                        skeleton = skeleton::input
                            .toAccessor()
                            .getOrInit { InputModel.Skeleton() },
                        dependencies = dependencies.input(),
                        onReady = ::onInput,
                    )
                )

                else -> QuestionStateModel.Error(
                    ErrorModel(
                        scope = stateScope,
                        skeleton = errorSkeleton,
                        dependencies = dependencies.error(),
                        onEnteredCorrect = { onAnswer(Answer.Incorrect) },
                        onTypo = ::onCorrect,
                        wordToLearn = skeleton.wordToLearn,
                        autoTTS = autoTTS,
                    )
                )
            }
        }

    val menu: StateFlow<MenuModel?> = skeleton
        .menuIsOpened
        .mapWithScope(scope) {menuScope, isOpened ->
            isOpened.ifTrue {
                MenuModel(
                    scope = menuScope,
                    dependencies = dependencies.menu(),
                    skeleton = skeleton::menu
                        .toAccessor()
                        .getOrInit { MenuModel.Skeleton() },
                    selectedSureness = skeleton.selectedSureness,
                    onAnswer = ::onAnswer,
                )
            }
        }

    fun switchMenuIsOpened() {
        menuIsOpened.update { !it }
    }

    private fun onInput(
        input: String,
    ) {
        val sureness = skeleton.selectedSureness.value
        when (input.normalized) {
            skeleton.wordToLearn.word.normalized -> onCorrect(
                sureness = sureness,
            )

            else -> skeleton.error.value = ErrorModel.Skeleton(
                incorrectInput = input,
                selectedSureness = sureness,
            )
        }
    }

    fun onAnswer(
        answer: Answer,
    ) {
        scope.launch {
            answeringInProgressRegistry.executeRegistered {
                onAnswer.invoke(answer)
            }
        }
    }

    private fun onCorrect(
        sureness: Sureness,
    ) {
        onAnswer(
            Answer.Correct(sureness)
        )
    }

    val title: String
        get() = skeleton
            .wordToLearn
            .let(dependencies.exerciseWords::get)
            .translation
}