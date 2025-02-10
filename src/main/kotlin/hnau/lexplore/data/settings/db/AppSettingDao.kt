package hnau.lexplore.data.settings.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppSettingDao {

    @Query("SELECT * FROM ${AppSetting.table}")
    fun getAll(): List<AppSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(
        setting: AppSetting,
    )
}