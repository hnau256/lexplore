package hnau.lexplore.ui.model.exercise

import hnau.lexplore.common.kotlin.Loadable
import hnau.lexplore.common.kotlin.coroutines.InProgressRegistry
import hnau.lexplore.common.kotlin.coroutines.flatMapState
import hnau.lexplore.common.kotlin.coroutines.mapState
import hnau.lexplore.common.kotlin.coroutines.mapWithScope
import hnau.lexplore.common.kotlin.coroutines.scopedInState
import hnau.lexplore.common.kotlin.ifNull
import hnau.lexplore.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.lexplore.common.model.goback.GoBackHandlerProvider
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.Engine
import hnau.lexplore.exercise.ExerciseWords
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.DictionaryWord
import hnau.lexplore.exercise.dto.Translation
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.dictionary.Dictionaries
import hnau.lexplore.exercise.dto.dictionary.DictionaryName
import hnau.lexplore.ui.model.question.QuestionModel
import hnau.shuffler.annotations.Shuffle
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ExerciseModel(
    private val scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
    private val goBack: () -> Unit,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        @Serializable(MutableStateFlowSerializer::class)
        val state: MutableStateFlow<State> = MutableStateFlow(
            State.Prepare(
                previousWord = null,
            )
        ),

        @Serializable(MutableStateFlowSerializer::class)
        val displayConfirmGoBack: MutableStateFlow<Boolean> = MutableStateFlow(false),

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

    @Shuffle
    interface Dependencies {

        val knowledgeRepository: KnowledgeRepository

        val dictionaries: Dictionaries

        fun question(
            exerciseWords: ExerciseWords,
        ): QuestionModel.Dependencies
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

    val displayConfirmGoBack: StateFlow<Boolean>
        get() = skeleton.displayConfirmGoBack

    private val inProgressRegistry = InProgressRegistry()

    val isInProgress: StateFlow<Boolean>
        get() = inProgressRegistry.isProgress

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
        answer: Answer,
    ) {
        val engine = engine.await()
        engine.answer(
            word = wordToLearn,
            answer = answer,
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
                    ),
                    skeleton = questionSkeleton,
                    onAnswer = { answer ->
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
                                answer = answer,
                            )
                        }
                    }
                )
            }
            Loadable.Ready(questionModelBuilder)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loadable.Loading,
        )
        .mapWithScope(scope) { stateScope, questionModelBuilderOrLoading ->
            questionModelBuilderOrLoading.map { questionModelBuilder ->
                questionModelBuilder(stateScope)
            }
        }

    fun confirmGoBack() = goBack()

    fun cancelGoBack() {
        skeleton.displayConfirmGoBack.value = false
    }

    override val goBackHandler: StateFlow<(() -> Unit)?> = question
        .scopedInState(scope)
        .flatMapState(scope) { (modelScope, questionModelOrLoading) ->
            questionModelOrLoading
                .orNull()
                ?.goBackHandler
                .ifNull { MutableStateFlow(null) }
                .mapState(modelScope) { goBack ->
                    goBack.ifNull {
                        {
                            skeleton
                                .displayConfirmGoBack
                                .update { displayConfirmGoBack -> !displayConfirmGoBack }
                        }
                    }
                }
        }
}