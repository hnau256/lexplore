package hnau.lexplore.exercise

import hnau.lexplore.common.kotlin.sumOf
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.KnowLevel
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
    ): List<Pair<WordToLearn, KnowLevel>> {
        val wordsWithInfo = wordsToLearn.map { word ->
            word to knowledgeRepository[word].value
        }
        val wordsToChooseWithInfo = wordsWithInfo.filter { (word, _) ->
            word != wordToLearnToExclude
        }
        val hasUnlearnedWord = wordsWithInfo.any { (_, info) ->
            info != null && info.forgettingFactor <= LearningConstants.initialForgettingFactor
        }
        val newWordToAdd = if (hasUnlearnedWord) {
            null
        } else {
            wordsToChooseWithInfo
                .firstOrNull { (_, info) -> info == null }
                ?.first
        }

        return wordsToChooseWithInfo
            .mapNotNull { (word, info) ->
                if (info == null && word != newWordToAdd) {
                    return@mapNotNull null
                }
                word to info.knowLevel
            }
            .ifEmpty {
                listOfNotNull(wordToLearnToExclude ?: wordsToLearn.firstOrNull())
                    .map { word ->
                        word to knowledgeRepository[word].value.knowLevel
                    }
            }
    }

    private fun Answer.calcNewForgettingFactor(
        current: ForgettingFactor,
    ): ForgettingFactor = when (this) {
        Answer.Correct -> current * LearningConstants.correctForgettingFactorFactor
        Answer.Incorrect -> current * LearningConstants.incorrectForgettingFactorFactor
        Answer.AlmostKnown -> LearningConstants.almostKnownForgettingFactor
        Answer.Useless -> LearningConstants.uselessForgettingFactor
    }.coerceAtLeast(
        minimumValue = LearningConstants.minForgettingFactor,
    )

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