package hnau.lexplore.light.engine

import android.content.Context
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.sumOf
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val currentWord: StateFlow<WordWithTranslation> = _currentWord.mapStateLite {
        WordWithTranslation(
            greek = it,
            russian = wordsWithTranslations.getValue(it),
        )
    }

    private fun updateLevel(
        newLevel: Float,
    ) {
        val word = _currentWord.value
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

    fun onAnswer(
        isCorrect: Boolean,
    ) {
        val word = _currentWord.value
        val currentLevel = levels.getOrDefault(word, 0f)
        val newLevel = when (isCorrect) {
            true -> currentLevel + (1 - currentLevel) * correctFactor
            false -> currentLevel * incorrectFactor
        }
        updateLevel(
            newLevel = newLevel
        )
    }

    fun markAsKnown() {
        updateLevel(
            newLevel = 1f,
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
                val knownLevel = levels.getOrDefault(word, 0f)
                val weight = 1f - knownLevel
                word to weight
            }

        val weightSum = wordsToChooseWithWeights.sumOf { it.second }
        val targetWeight = Random.nextFloat() * weightSum
        var currentWeight = 0f
        wordsToChooseWithWeights.forEach { (word, weight) ->
            currentWeight += weight
            if (currentWeight > targetWeight) {
                return word
            }
        }
        return words
            .dropLastWhile { it == currentWord }
            .last()
    }


    companion object {

        private const val wordsWindow: Int = 10
        private const val wellKnownLevel: Float = 0.75f
        private const val correctFactor: Float = 0.2f
        private const val incorrectFactor: Float = 0.5f
    }
}