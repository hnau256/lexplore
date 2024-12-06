package hnau.lexplore.light.engine

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao as RoomDao
import androidx.room.Database as DatabaseAnnotation


@Entity(
    tableName = KnowledgeLevel.tableName,
)
data class KnowledgeLevel(
    @PrimaryKey
    @ColumnInfo(name = "greek_word")
    val greekWord: String,

    @ColumnInfo(name = "fraction")
    val fraction: Float,
) {

    @RoomDao
    interface Dao {

        @Query("SELECT * FROM $tableName")
        fun getAll(): List<KnowledgeLevel>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(
            level: KnowledgeLevel,
        )

        companion object {

            fun create(
                context: Context,
            ): Dao = Room
                .databaseBuilder(
                    context,
                    Database::class.java,
                    "lexplorer_5"
                )
                .allowMainThreadQueries()
                .build()
                .dao()
        }
    }

    @DatabaseAnnotation(
        entities = [KnowledgeLevel::class],
        version = 1,
    )
    abstract class Database : RoomDatabase() {

        abstract fun dao(): Dao
    }

    companion object {

        const val tableName = "knowledge_level"
    }
}