package hnau.lexplore.model.exercise

import hnau.lexplore.common.DictionaryWord
import hnau.lexplore.common.Translation
import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.dictionary.Dictionaries
import hnau.lexplore.common.dictionary.DictionaryName
import hnau.lexplore.common.exercise.Engine
import hnau.lexplore.common.exercise.ExerciseWords
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings
import hnau.lexplore.common.settings.Setting
import hnau.lexplore.common.settings.map
import hnau.lexplore.common.speaker.Speaker
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.model.exercise.question.QuestionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.Loading
import org.hnau.commons.kotlin.Ready
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapStateLite
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import arrow.core.getOrElse
import org.hnau.commons.kotlin.map
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.stringToBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class ExerciseModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val state: MutableStateFlow<State> = MutableStateFlow(
            State.Prepare(
                previousWord = null,
            )
        ),


        val dictionaries: Set<DictionaryName>,
    )

    @Serializable
    sealed interface State {

        @Serializable
        @SerialName("prepare")
        data class Prepare(
            val previousWord: WordToLearn?,
        ) : State

        @Serializable
        @SerialName("question")
        data class Question(
            val question: QuestionModel.Skeleton,
        ) : State
    }

    @Pipe
    interface Dependencies {

        val appSettings: AppSettings

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        val speaker: Speaker

        fun question(
            autoTTS: AutoTTS,
            exerciseWords: ExerciseWords,
        ): QuestionModel.Dependencies
    }

    private val autoTTSSetting: Setting<Boolean> = dependencies
        .appSettings["auto_tts"]
        .map(Mapper.stringToBoolean)

    val autoTTS: AutoTTS = AutoTTS(
        active = autoTTSSetting
            .state
            .mapStateLite { it.getOrElse { true } },
    )

    val switchAutoTTS: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(scope) {
            autoTTSSetting.update(!autoTTS.active.value)
        }

    private val exerciseWordsMap: Deferred<Map<WordToLearn, Translation>> = scope.async {
        withContext(Dispatchers.Default) {
            skeleton
                .dictionaries
                .flatMap { dictionaryName ->
                    dependencies
                        .dictionaries[dictionaryName]
                        .dictionaryWords
                }
                .sortedByDescending(DictionaryWord::weight)
                .associate { dictionaryWord ->
                    dictionaryWord.toLearn to dictionaryWord.translation
                }
        }
    }

    private val inProgressRegistry = InProgressRegistry(scope)

    val isInProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    private val engine: Deferred<Engine> = scope.async {
        Engine(
            wordsToLearn = exerciseWordsMap.await().map { it.key },
            knowledgeRepository = dependencies.knowledgeRepository,
        )
    }

    init {
        scope.launch {
            skeleton
                .state
                .collectLatest { state ->
                    when (state) {
                        is State.Prepare -> switchToNewQuestion(
                            previousWord = state.previousWord,
                        )

                        is State.Question -> Unit
                    }
                }
        }
    }

    private suspend fun switchToNewQuestion(
        previousWord: WordToLearn?,
    ) {
        inProgressRegistry.executeRegistered {
            val engine = engine.await()
            val newWordToLearn = engine.findNextWordToLearn(
                wordToLearnToExclude = previousWord,
            )
            skeleton.state.value = State.Question(
                question = QuestionModel.Skeleton(
                    wordToLearn = newWordToLearn,
                    previousWord = MutableStateFlow(previousWord),
                )
            )
        }
    }

    private suspend fun answer(
        wordToLearn: WordToLearn,
        rating: FsrsRating,
    ) {
        val engine = engine.await()
        engine.answer(
            word = wordToLearn,
            rating = rating,
        )
        skeleton.state.value = State.Prepare(
            previousWord = wordToLearn,
        )
    }

    val question: StateFlow<Loadable<QuestionModel>> = skeleton
        .state
        .mapNotNull { state ->
            when (state) {
                is State.Question -> state.question
                is State.Prepare -> null
            }
        }
        .map { questionSkeleton ->
            val exerciseWords = exerciseWordsMap.await().let(::ExerciseWords)
            val questionModelBuilder: (CoroutineScope) -> QuestionModel = { stateScope ->
                QuestionModel(
                    scope = stateScope,
                    dependencies = dependencies.question(
                        exerciseWords = exerciseWords,
                        autoTTS = autoTTS,
                    ),
                    skeleton = questionSkeleton,
                    onAnswer = { rating ->
                        val currentQuestionWord: WordToLearn? = skeleton
                            .state
                            .value
                            .let { localState ->
                                when (localState) {
                                    is State.Question -> localState.question.wordToLearn
                                    is State.Prepare -> null
                                }
                            }
                        val wordToLearn = questionSkeleton.wordToLearn
                        if (wordToLearn == currentQuestionWord) {
                            answer(
                                wordToLearn = wordToLearn,
                                rating = rating,
                            )
                        }
                    }
                )
            }
            Ready(questionModelBuilder)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loading,
        )
        .mapWithScope(scope) { stateScope, questionModelBuilderOrLoading ->
            questionModelBuilderOrLoading.map { questionModelBuilder ->
                questionModelBuilder(stateScope)
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}
