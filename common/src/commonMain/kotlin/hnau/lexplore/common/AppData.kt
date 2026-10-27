package hnau.lexplore.common

import hnau.lexplore.common.knowledge.KnowledgeRepository
import hnau.lexplore.common.settings.AppSettings

data class AppData(
    val knowledgeRepository: KnowledgeRepository,
    val appSettings: AppSettings,
)

fun interface AppDataFactory {

    suspend fun create(): AppData
}
