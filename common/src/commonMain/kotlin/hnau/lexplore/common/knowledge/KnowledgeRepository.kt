package hnau.lexplore.common.knowledge

import hnau.lexplore.common.WordToLearn
import hnau.lexplore.fsrs.dto.WordInfo
import kotlinx.coroutines.flow.StateFlow

interface KnowledgeRepository {

    operator fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?>

    suspend fun update(
        key: WordToLearn,
        info: WordInfo,
    )
}
