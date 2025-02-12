package hnau.lexplore.exercise

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.lexplore.common.kotlin.sumOf
import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.Sureness
import hnau.lexplore.exercise.dto.Word
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class Engine(
    private val words: List<Word>,
    private val knowledgeRepository: KnowledgeRepository,
) {

    suspend fun generateNewQuestion(
        wordToExclude: WordToLearn?,
    ): Question = withContext(Dispatchers.Default) {
        val wordsToChooseFrom = collectWordsToChoose(
            wordToExclude = wordToExclude,
        )
        val word = wordsToChooseFrom.chooseRandom { (_, info) ->
            info.weight
        }
        Question(
            word = word.first,
            info = word.second,
            answer = { answer ->
                val newForgettingFactor = answer.calcNewForgettingFactor(
                    current = word
                        .second
                        ?.forgettingFactor
                        ?: LearningConstants.initialForgettingFactor,
                )
                knowledgeRepository.update(
                    key = word.first.toLearn,
                    newForgettingFactor = newForgettingFactor,
                )
            }
        )
    }

    private fun collectWordsToChoose(
        wordToExclude: WordToLearn?,
    ): NonEmptyList<Pair<Word, WordInfo?>> {

        fun collectAnsweredWordInfos(
            filterByKnowLevel: Boolean,
        ): NonEmptyList<Pair<Word, WordInfo>>? = words
            .asSequence()
            .filter { it.toLearn != wordToExclude }
            .mapNotNull { word ->
                val wordInfo = knowledgeRepository
                    .get(word.toLearn)
                    .value
                    ?: return@mapNotNull null
                word to wordInfo
            }
            .filter { (_, info) ->
                !filterByKnowLevel ||
                        info.knowLevel <= LearningConstants.maxKnowLevelToAsk
            }
            .toList()
            .toNonEmptyListOrNull()

        val answeredWordsWithLowKnowLevel = collectAnsweredWordInfos(
            filterByKnowLevel = true,
        )
        if (answeredWordsWithLowKnowLevel != null) {
            return answeredWordsWithLowKnowLevel
        }

        val newWord = words
            .asSequence()
            .filter { it.toLearn != wordToExclude }
            .firstOrNull { word ->
                val info = knowledgeRepository.get(word.toLearn).value
                info == null
            }
        if (newWord != null) {
            return nonEmptyListOf(newWord to null)
        }

        val answeredWords = collectAnsweredWordInfos(
            filterByKnowLevel = false,
        )
        if (answeredWords != null) {
            return answeredWords
        }

        error("Unable find word to learn")
    }

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

    private inline fun <T> NonEmptyList<T>.chooseRandom(
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