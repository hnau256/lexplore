package hnau.lexplore.common.exercise

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.fsrs.Fsrs
import hnau.lexplore.fsrs.LearningConstants
import hnau.lexplore.fsrs.dto.FsrsRating
import hnau.lexplore.fsrs.dto.Retrievability
import hnau.lexplore.fsrs.dto.WordInfo
import hnau.lexplore.fsrs.retrievability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.ifTrue
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
        val retention: Retrievability,
    ) {

        val weight: Double
            get() = (-ln(retention.value))
                .coerceAtLeast(0.0)
                .pow(LearningConstants.priorityPow)
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
        rating: FsrsRating,
    ) {
        val now = Clock.System.now()
        val newInfo = knowledgeRepository[word].value.foldNullable(
            ifNull = { Fsrs.initialState(rating = rating, now = now) },
            ifNotNull = { info ->
                Fsrs.review(
                    info = info,
                    rating = rating,
                    now = now,
                )
            },
        )
        knowledgeRepository.update(
            key = word,
            info = newInfo,
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
            info.foldNullable(
                ifNull = { null },
                ifNotNull = { startedInfo ->
                    Candidate(
                        word = word,
                        info = startedInfo,
                        retention = startedInfo.retrievability(now),
                    )
                },
            )
        }
        val nextNewWord = wordsWithInfo
            .firstOrNull { (_, info) -> info == null }
            ?.first

        candidates.isEmpty().ifTrue {
            return nextNewWord.ifNull { error("There are no words to learn") }
        }

        val newWordProbability = nextNewWord.foldNullable(
            ifNull = { 0.0 },
            ifNotNull = {
                val retentionReference = softmin(
                    values = candidates.map { candidate -> candidate.retention.value },
                    tau = LearningConstants.softminTau,
                )
                (
                    LearningConstants.newWordProbabilityK *
                        retentionReference.pow(LearningConstants.newWordRetentionPow)
                    )
                    .coerceIn(0.0, 1.0)
            },
        )

        (nextNewWord != null && Random.nextDouble() < newWordProbability).ifTrue {
            return nextNewWord.ifNull { error("There are no words to learn") }
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
                (candidate.word == previousWord).foldBoolean(
                    ifTrue = { weight * LearningConstants.previousWordPenalty },
                    ifFalse = { weight },
                )
            }
            candidate.word to weight
        }
        val totalWeight = weightedWords.sumOf { (_, weight) -> weight }
        (totalWeight <= 0.0).ifTrue {
            return candidates
                .minByOrNull { candidate -> candidate.info.stability.duration }
                ?.word
                .ifNull { candidates.first().word }
        }
        val targetWeight = Random.nextDouble() * totalWeight
        var currentWeight = 0.0
        weightedWords.forEach { (word, weight) ->
            currentWeight += weight
            (currentWeight >= targetWeight).ifTrue {
                return word
            }
        }
        return weightedWords.last().first
    }

    private fun softmin(
        values: List<Double>,
        tau: Double,
    ): Double {
        val min = values.minOrNull().ifNull { return 0.0 }
        val meanExp = values.sumOf { value ->
            exp(-(value - min) / tau)
        } / values.size
        return (min - tau * ln(meanExp)).coerceIn(0.0, 1.0)
    }
}
