package hnau.lexplore.data.knowledge

import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.data.knowledge.db.KnowledgeLevelOfWord
import hnau.lexplore.exercise.dto.Difficulty
import hnau.lexplore.exercise.dto.Stability
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days

class KnowledgeRepositoryImpl private constructor(
    initial: Map<WordToLearn, WordInfo>,
    private val persist: suspend (WordToLearn, WordInfo) -> Unit,
) : KnowledgeRepository {

    private val infos: MutableMap<WordToLearn, MutableStateFlow<WordInfo?>> =
        initial
            .mapValues { (_, value) -> MutableStateFlow<WordInfo?>(value) }
            .toMutableMap()

    override operator fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?> = synchronized(infos) {
        infos.getOrPut(key) {
            MutableStateFlow(null)
        }
    }

    private val updateMutex = Mutex()

    override suspend fun update(
        key: WordToLearn,
        info: WordInfo,
    ) {
        updateMutex.withLock {
            synchronized(infos) {
                infos[key] = when (val stateFlow = infos[key]) {
                    null -> MutableStateFlow(info)
                    else -> stateFlow.apply { value = info }
                }
            }
            withContext(Dispatchers.IO) {
                persist(key, info)
            }
        }
    }

    companion object {

        suspend fun create(
            database: AppDatabase,
        ): KnowledgeRepository {
            val dao = database.knowledgeDao()

            val initialList: List<KnowledgeLevelOfWord> =
                withContext(Dispatchers.IO) { dao.getAll() }

            val initial: Map<WordToLearn, WordInfo> = withContext(Dispatchers.Default) {
                initialList.associate { knowledge ->
                    knowledge.question to WordInfo(
                        stability = Stability(knowledge.stability.days),
                        difficulty = Difficulty(knowledge.difficulty),
                        lastAnswerTimestamp = knowledge.lastAnswerTimestamp,
                    )
                }
            }

            return KnowledgeRepositoryImpl(
                initial = initial,
                persist = { question, info ->
                    dao.insert(
                        knowledge = KnowledgeLevelOfWord(
                            question = question,
                            lastAnswerTimestamp = info.lastAnswerTimestamp,
                            stability = info.stability.days,
                            difficulty = info.difficulty.value,
                        )
                    )
                }
            )
        }

        internal fun createForTest(
            initial: Map<WordToLearn, WordInfo> = emptyMap(),
            persist: suspend (WordToLearn, WordInfo) -> Unit,
        ): KnowledgeRepository = KnowledgeRepositoryImpl(
            initial = initial,
            persist = persist,
        )
    }
}
