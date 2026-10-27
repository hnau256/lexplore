package hnau.lexplore.data.knowledge

import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.Stability
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class KnowledgeRepositoryUpdateTest {

    @Test
    fun `update persists once and updates in-memory state`() = runBlocking {
        val word = WordToLearn("test")
        val info = WordInfo(
            stability = Stability(1.0),
            difficulty = Difficulty(5.0),
            lastAnswerTimestamp = Instant.fromEpochMilliseconds(0),
        )
        var persistCalls = 0
        var persistedInfo: WordInfo? = null
        val repository = KnowledgeRepositoryImpl.createForTest(
            persist = { _, value ->
                persistCalls += 1
                persistedInfo = value
            },
        )

        withTimeout(1_000) {
            repository.update(
                key = word,
                info = info,
            )
        }

        assertEquals(1, persistCalls)
        assertEquals(info, persistedInfo)
        assertEquals(info, repository[word].value)
    }
}
