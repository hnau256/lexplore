package hnau.lexplore.data.knowledge

import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.data.knowledge.db.KnowledgeLevelOfWord
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class KnowledgeRepositoryImpl private constructor(
    initial: Map<WordToLearn, WordInfo>,
    private val update: suspend (WordToLearn, WordInfo) -> Unit,
) : KnowledgeRepository {

    private val infos: MutableMap<WordToLearn, MutableStateFlow<WordInfo?>> =
        initial
            .mapValues { (_, value) -> MutableStateFlow<WordInfo?>(value) }
            .toMutableMap()

    override fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?> = synchronized(infos) {
        infos.getOrPut(key) {
            MutableStateFlow(null)
        }
    }

    private val updateMutex = Mutex()

    override suspend fun update(
        key: WordToLearn,
        newForgettingFactor: ForgettingFactor,
    ) {
        val wordInfo = WordInfo(
            forgettingFactor = newForgettingFactor,
            lastAnswerTimestamp = Clock.System.now(),
        )
        updateMutex.withLock {
            synchronized(infos) {
                infos[key] = when (val stateFlow = infos[key]) {
                    null -> MutableStateFlow(wordInfo)
                    else -> stateFlow.apply { value = wordInfo }
                }
            }
            update(key, wordInfo)
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
                initialList.associate { questionInfoView ->
                    questionInfoView.question to WordInfo(
                        forgettingFactor = questionInfoView.level,
                        lastAnswerTimestamp = questionInfoView.lastAnswerTimestamp,
                    )
                }
            }

            return KnowledgeRepositoryImpl(
                initial = initial,
                update = { question, info ->
                    dao.insert(
                        level = KnowledgeLevelOfWord(
                            question = question,
                            level = info.forgettingFactor,
                            lastAnswerTimestamp = info.lastAnswerTimestamp,
                        )
                    )
                }
            )
        }
    }
}