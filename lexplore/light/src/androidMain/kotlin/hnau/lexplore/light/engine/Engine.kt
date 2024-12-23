package hnau.lexplore.light.engine

import android.content.Context
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.sumOf
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.pow
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class Engine(
    context: Context,
) {

    private val wordsWithTranslations: Map<String, String> = context
        .assets
        .open("dictionary.txt")
        .use { inputStream ->
            inputStream
                .reader()
                .use { reader ->
                    reader
                        .readLines()
                        .associate { line ->
                            val wordWithTranslation = line
                                .split('\t')
                                .map { it.trim() }
                            if (wordWithTranslation.size != 2) {
                                error("Expected word with translation got '$line'")
                            }
                            val (word, translation) = wordWithTranslation
                            word to translation
                        }
                }
        }


    private val words: List<String> = wordsWithTranslations
        .keys
        .toList()

    private val knowledgeLevelDao: KnowledgeLevel.Dao = KnowledgeLevel.Dao.create(
        context = context,
    )

    private val levels: MutableMap<String, Float> = knowledgeLevelDao
        .getAll()
        .associate { level -> level.greekWord to level.fraction }
        .toMutableMap()

    private val _currentWord: MutableStateFlow<String> = MutableStateFlow(
        resolveNextWord(
            currentWord = null,
        )
    )

    val currentWord: StateFlow<WordWithTranslation> = _currentWord.mapStateLite { greek ->
        WordWithTranslation(
            greek = greek,
            russian = wordsWithTranslations.getValue(greek),
            knowledgeLevel = getLevel(greek)
        )
    }

    enum class Sureness(
        val factor: Float,
    ) {
        Low(
            factor = 0.1f,
        ),
        Medium(
            factor = 0.3f,
        ),
        Height(
            factor = 1f,
        );

        companion object {

            val primary: Sureness = Medium
        }
    }

    sealed interface Result {
        data object Incorrect : Result

        data object Useless : Result

        data class Correct(
            val sureness: Sureness,
        ) : Result
    }

    fun onResult(
        result: Result,
    ) {
        val word = _currentWord.value
        val currentLevel = getLevel(word)
        val newLevel = when (result) {
            is Result.Correct -> currentLevel + (1 - currentLevel) * correctFactor * result.sureness.factor
            Result.Incorrect -> currentLevel * incorrectFactor
            Result.Useless -> 1f
        }
        levels[word] = newLevel
        knowledgeLevelDao.insert(
            KnowledgeLevel(
                greekWord = word,
                fraction = newLevel,
            )
        )
        _currentWord.value = resolveNextWord(
            currentWord = word,
        )
    }

    private fun resolveNextWord(
        currentWord: String?,
    ): String {
        val wellKnownWordsCount = levels.count { (_, level) ->
            level >= wellKnownLevel
        }
        val wordsToChooseCount = (wellKnownWordsCount + wordsWindow).coerceAtMost(words.lastIndex)

        val wordsToChooseWithWeights = words
            .take(wordsToChooseCount)
            .filter { it != currentWord }
            .map { word ->
                val knownLevel = getLevel(word)
                val weight = 1f - knownLevel
                word to weight
            }

        val weightSum = wordsToChooseWithWeights.sumOf { it.second }
        val targetWeight = Random.nextFloat() * weightSum
        var currentWeight = 0f
        wordsToChooseWithWeights.forEach { (word, weight) ->
            currentWeight += weight
            if (currentWeight >= targetWeight) {
                return word
            }
        }
        return words
            .dropLastWhile { it == currentWord }
            .last()
    }

    private fun getLevel(word: String): Float = levels[word] ?: 0f

    companion object {

        private const val wordsWindow: Int = 10
        private const val wellKnownLevel: Float = 0.75f
        private const val correctFactor: Float = 0.5f
        private const val incorrectFactor: Float = 0.5f
    }
}