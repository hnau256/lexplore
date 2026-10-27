package hnau.lexplore.data

import android.content.Context
import hnau.lexplore.common.AppData
import hnau.lexplore.common.AppDataFactory
import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.data.knowledge.KnowledgeRepositoryImpl
import hnau.lexplore.data.settings.AppSettingsImpl

class AndroidAppDataFactory(
    private val context: Context,
) : AppDataFactory {

    override suspend fun create(): AppData {
        val database = AppDatabase.create(context)
        return AppData(
            knowledgeRepository = KnowledgeRepositoryImpl.create(database),
            appSettings = AppSettingsImpl.create(database),
        )
    }
}
