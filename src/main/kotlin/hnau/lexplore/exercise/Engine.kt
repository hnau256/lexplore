package hnau.lexplore.exercise

import hnau.lexplore.data.knowledge.KnowledgeRepository
import hnau.lexplore.exercise.dto.Answer
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import hnau.lexplore.exercise.dto.forgettingFactor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Clock

class Engine(
    private val wordsToLearn: List<WordToLearn>,
    private val knowledgeRepository: KnowledgeRepository,
) {

    private data class Candidate(
        val word: WordToLearn,
        val info: WordInfo,
        val overdueRatio: Double,
    ) {

        val retention: Double
            get() = exp(-overdueRatio)

        val weight: Double
            get() = overdueRatio.pow(LearningConstants.priorityPow)
    }

    suspend fun findNextWordToLearn(
        wordToLearnToExclude: WordToLearn?,
    ): WordToLearn = withContext(Dispatchers.Default) {
        chooseWord(
            wordToLearnToExclude = wordToLearnToExclude,
        )
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

    private fun chooseWord(
        wordToLearnToExclude: WordToLearn?,
    ): WordToLearn {
        val now = Clock.System.now()
        val wordsWithInfo = wordsToLearn.map { word ->
            word to knowledgeRepository[word].value
        }
        val candidates = wordsWithInfo.mapNotNull { (word, info) ->
            info?.let { startedInfo ->
                Candidate(
                    word = word,
                    info = startedInfo,
                    overdueRatio = startedInfo.overdueRatio(now),
                )
            }
        }
        val nextNewWord = wordsWithInfo
            .firstOrNull { (_, info) -> info == null }
            ?.first

        if (candidates.isEmpty()) {
            return nextNewWord ?: error("There are no words to learn")
        }

        val hasUnlearnedWord = candidates.any { candidate ->
            candidate.info.forgettingFactor <= LearningConstants.initialForgettingFactor
        }

        val newWordProbability = if (nextNewWord == null || hasUnlearnedWord) {
            0.0
        } else {
            val retentionReference = softmin(
                values = candidates.map { candidate -> candidate.retention },
                tau = LearningConstants.softminTau,
            )
            val newWordProbabilityValue = LearningConstants.newWordProbabilityK *
                retentionReference.pow(LearningConstants.newWordRetentionPow)
            newWordProbabilityValue.coerceIn(0.0, 1.0)
        }

        if (nextNewWord != null && Random.nextDouble() < newWordProbability) {
            return nextNewWord
        }

        return chooseOldWord(
            candidates = candidates,
            previousWord = wordToLearnToExclude,
        )
    }

    private fun chooseOldWord(
        candidates: List<Candidate>,
        previousWord: WordToLearn?,
    ): WordToLearn {
        val weightedWords = candidates.map { candidate ->
            val weight = candidate.weight.let { weight ->
                if (candidate.word == previousWord) {
                    weight * LearningConstants.previousWordPenalty
                } else {
                    weight
                }
            }
            candidate.word to weight
        }
        val totalWeight = weightedWords.sumOf { (_, weight) -> weight }
        if (totalWeight <= 0.0) {
            return candidates
                .minByOrNull { candidate -> candidate.info.forgettingFactor.factor }
                ?.word
                ?: candidates.first().word
        }
        val targetWeight = Random.nextDouble() * totalWeight
        var currentWeight = 0.0
        weightedWords.forEach { (word, weight) ->
            currentWeight += weight
            if (currentWeight >= targetWeight) {
                return word
            }
        }
        return weightedWords.last().first
    }

    private fun softmin(
        values: List<Double>,
        tau: Double,
    ): Double {
        val min = values.minOrNull() ?: return 0.0
        val meanExp = values.sumOf { value ->
            exp(-(value - min) / tau)
        } / values.size
        return (min - tau * ln(meanExp)).coerceIn(0.0, 1.0)
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
}
