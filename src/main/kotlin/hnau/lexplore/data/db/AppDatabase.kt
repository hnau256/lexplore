package hnau.lexplore.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hnau.lexplore.data.knowledge.db.KnowledgeDao
import hnau.lexplore.data.knowledge.db.KnowledgeDatabaseTypeConverters
import hnau.lexplore.data.knowledge.db.KnowledgeLevelOfWord
import hnau.lexplore.data.settings.db.AppSetting
import hnau.lexplore.data.settings.db.AppSettingDao

@Database(
    entities = [
        KnowledgeLevelOfWord::class,
        AppSetting::class,
    ],
    version = 1,
)
@TypeConverters(KnowledgeDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun knowledgeDao(): KnowledgeDao

    abstract fun appSettingDao(): AppSettingDao

    companion object {

        fun create(
            context: Context,
        ): AppDatabase = Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "lexplorer"
            )
            .build()
    }
}