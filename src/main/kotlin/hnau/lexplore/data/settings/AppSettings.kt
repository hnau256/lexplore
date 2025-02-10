package hnau.lexplore.data.settings

import hnau.lexplore.data.db.AppDatabase

interface AppSettings {

    operator fun get(
        key: String,
    ): Setting<String>

    companion object {

        suspend fun create(
            database: AppDatabase,
        ): AppSettings = AppSettingsImpl.create(
            database = database,
        )
    }
}