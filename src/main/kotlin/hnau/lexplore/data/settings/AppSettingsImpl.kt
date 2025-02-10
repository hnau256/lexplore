package hnau.lexplore.data.settings

import arrow.core.getOrNone
import hnau.lexplore.data.db.AppDatabase
import hnau.lexplore.data.settings.db.AppSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppSettingsImpl(
    private val initial: Map<String, String>,
    private val update: suspend (key: String, value: String) -> Unit,
) : AppSettings {

    private val settings: MutableMap<String, Setting<String>> =
        mutableMapOf()

    override fun get(
        key: String,
    ): Setting<String> = settings.getOrPut(
        key = key,
    ) {
        SettingImpl(
            initialValue = initial.getOrNone(key),
            update = {update(key, it)}
        )
    }

    companion object {

        suspend fun create(
            database: AppDatabase,
        ): AppSettingsImpl {
            val dao = database.appSettingDao()
            val initial = withContext(Dispatchers.IO) {
                dao
                    .getAll()
                    .associate { setting ->
                        setting.key to setting.value
                    }
            }
            return AppSettingsImpl(
                initial = initial,
                update = { key, value ->
                    withContext(Dispatchers.IO) {
                        dao.insert(
                            AppSetting(
                                key = key,
                                value = value
                            )
                        )
                    }
                }
            )
        }
    }
}