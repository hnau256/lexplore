package hnau.lexplore.light.engine

import android.content.Context
import android.util.Log
import hnau.common.kotlin.sumOf
import hnau.lexplore.light.Word
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class Engine(
    context: Context,
) {

    private val words: List<String> = Word
        .loadList(context)
        .sortedByDescending(Word::count)
        .map(Word::word)

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

    val currentWord: StateFlow<String>
        get() = _currentWord

    private fun updateLevel(
        newLevel: Float,
    ) {
        val word = currentWord.value
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
        val word = currentWord.value
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

        private const val wordsWindow: Int = 5
        private const val wellKnownLevel: Float = 0.75f
        private const val correctFactor: Float = 0.3f
        private const val incorrectFactor: Float = 0.5f
    }
}