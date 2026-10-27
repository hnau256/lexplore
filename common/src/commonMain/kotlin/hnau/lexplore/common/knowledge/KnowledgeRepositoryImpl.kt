package hnau.lexplore.common.knowledge

import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.lexplore.common.WordToLearn
import org.hnau.commons.kotlin.foldNullable
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
            .mapValues { (_, value) -> value.toMutableStateFlowAsInitial<WordInfo?>() }
            .toMutableMap()

    private val updateMutex = Mutex()

    override operator fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?> = synchronized(lock) {
        infos.getOrPut(key) {
            null.toMutableStateFlowAsInitial<WordInfo?>()
        }
    }

    override suspend fun update(
        key: WordToLearn,
        info: WordInfo,
    ) {
        updateMutex.withLock {
            synchronized(lock) {
                infos[key] = infos[key].foldNullable(
                    ifNull = { info.toMutableStateFlowAsInitial() },
                    ifNotNull = { stateFlow -> stateFlow.apply { value = info } },
                )
            }
            persist(key, info)
        }
    }
}
