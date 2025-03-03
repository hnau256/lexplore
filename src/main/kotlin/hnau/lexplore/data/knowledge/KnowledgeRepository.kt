package hnau.lexplore.data.knowledge

import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.exercise.dto.ForgettingFactor
import hnau.lexplore.exercise.dto.WordInfo
import hnau.lexplore.exercise.dto.WordToLearn
import kotlinx.coroutines.flow.StateFlow

interface KnowledgeRepository {

    operator fun get(
        key: WordToLearn,
    ): StateFlow<WordInfo?>

    suspend fun update(
        key: WordToLearn,
        newForgettingFactor: ForgettingFactor,
    )

    companion object {

        suspend fun create(
            database: AppDatabase,
        ): KnowledgeRepository = KnowledgeRepositoryImpl.create(
            database = database,
        )
    }
}