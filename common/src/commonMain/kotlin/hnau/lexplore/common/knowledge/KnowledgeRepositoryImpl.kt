package hnau.lexplore.common.knowledge

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.fsrs.dto.WordInfo
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KnowledgeRepositoryImpl(
    initial: Map<WordToLearn, WordInfo>,
    private val persist: suspend (WordToLearn, WordInfo) -> Unit,
) : KnowledgeRepository {

    private val lock = SynchronizedObject()

    private val infos: MutableMap<WordToLearn, MutableStateFlow<WordInfo?>> =
        initial
            .mapValues { (_, value) -> MutableStateFlow<WordInfo?>(value) }
            .toMutableMap()

    private val updateMutex = Mutex()

    override operator fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?> = synchronized(lock) {
        infos.getOrPut(key) {
            MutableStateFlow(null)
        }
    }

    override suspend fun update(
        key: WordToLearn,
        info: WordInfo,
    ) {
        updateMutex.withLock {
            synchronized(lock) {
                infos[key] = when (val stateFlow = infos[key]) {
                    null -> MutableStateFlow(info)
                    else -> stateFlow.apply { value = info }
                }
            }
            persist(key, info)
        }
    }
}
