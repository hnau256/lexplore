package hnau.lexplore.data.knowledge.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KnowledgeDao {

    @Query("SELECT * FROM ${KnowledgeLevelOfWord.table}")
    fun getAll(): List<KnowledgeLevelOfWord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(
        level: KnowledgeLevelOfWord,
    )
}