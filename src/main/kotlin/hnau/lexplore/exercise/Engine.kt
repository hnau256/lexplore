package hnau.lexplore.exercise

import hnau.lexplore.common.kotlin.sumOf
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.KnowLevel
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.forgettingFactor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.random.Random

class Engine(
    private val wordsToLearn: List<WordToLearn>,
    private val knowledgeRepository: KnowledgeRepository,
) {

    suspend fun findNextWordToLearn(
        wordToLearnToExclude: WordToLearn?,
    ): WordToLearn = withContext(Dispatchers.Default) {
        val (word) = collectWordsToChoose(
            wordToLearnToExclude = wordToLearnToExclude,
        ).chooseRandom { (_, info) ->
            info.chooseWeight
        }
        word
    }

    suspend fun answer(
        word: WordToLearn,
        answer: Answer,
    ) {
        val info = knowledgeRepository[word].value
        val newForgettingFactor = answer.calcNewForgettingFactor(
            current = info.forgettingFactor,
        )
        knowledgeRepository.update(
            key = word,
            newForgettingFactor = newForgettingFactor,
        )
    }

    private val KnowLevel.chooseWeight: Float
        get() = (1f - level).pow(LearningConstants.weightPow)

    private fun collectWordsToChoose(
        wordToLearnToExclude: WordToLearn?,
    ): List<Pair<WordToLearn, KnowLevel>> = wordsToLearn
        .fold(
            initial = false to emptyList<Pair<WordToLearn, KnowLevel>>(),
        ) { (collectedUnknown, alreadyCollected), word ->
            if (word == wordToLearnToExclude) {
                return@fold collectedUnknown to alreadyCollected
            }
            val info = knowledgeRepository[word].value
            val result = word to info.knowLevel
            val answeredCorrectOnce =
                info.forgettingFactor > LearningConstants.initialForgettingFactor
            when (answeredCorrectOnce) {
                false -> true to when (collectedUnknown) {
                    true -> alreadyCollected
                    false -> alreadyCollected + result
                }

                true -> collectedUnknown to (alreadyCollected + result)
            }
        }
        .second

    private fun Answer.calcNewForgettingFactor(
        current: ForgettingFactor,
    ): ForgettingFactor = when (this) {
        is Answer.Correct -> current *
                LearningConstants.correctForgettingFactorFactor *
                sureness.factor

        Answer.Incorrect -> current * LearningConstants.incorrectForgettingFactorFactor
        Answer.AlmostKnown -> LearningConstants.almostKnownForgettingFactor
        Answer.Useless -> LearningConstants.uselessForgettingFactor
    }.coerceAtLeast(
        minimumValue = LearningConstants.minForgettingFactor,
    )

    private val Sureness.factor: Float
        get() = when (this) {
            Sureness.Low -> 0.5f
            Sureness.Medium -> 1f
            Sureness.Height -> 2f
        }

    private inline fun <T> List<T>.chooseRandom(
        weight: (T) -> Float,
    ): T {
        val withWeights = map { item ->
            item to weight(item)
        }
        val targetWeight = Random.nextFloat() * withWeights.sumOf { it.second }
        var currentSum = 0f
        withWeights.forEach { (item, weight) ->
            currentSum += weight
            if (currentSum >= targetWeight) {
                return item
            }
        }
        error("This shouldn't have happened")
    }
}