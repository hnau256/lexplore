package hnau.lexplore.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
)
@TypeConverters(KnowledgeDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun knowledgeDao(): KnowledgeDao

    abstract fun appSettingDao(): AppSettingDao

    companion object {

        private val migration1To2 = object : Migration(1, 2) {

            override fun migrate(
                db: SupportSQLiteDatabase,
            ) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS knowledge_level_new (
                        question TEXT NOT NULL,
                        last_answer_timestamp INTEGER NOT NULL,
                        stability REAL NOT NULL,
                        difficulty REAL NOT NULL,
                        PRIMARY KEY(question)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO knowledge_level_new (
                        question,
                        last_answer_timestamp,
                        stability,
                        difficulty
                    )
                    SELECT
                        question,
                        last_answer_timestamp,
                        CASE WHEN level >= 1000000.0 THEN 36500.0 ELSE level / 1440.0 END,
                        5.0
                    FROM knowledge_level
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE knowledge_level")
                db.execSQL("ALTER TABLE knowledge_level_new RENAME TO knowledge_level")
            }
        }

        fun create(
            context: Context,
        ): AppDatabase = Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "lexplorer"
            )
            .addMigrations(migration1To2)
            .build()
    }
}
