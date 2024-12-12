package hnau.lexplore.android.data.dictionary.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DictionaryWithValues::class,
    ],
    views = [
        DictionaryInfoView::class,
    ],
    version = 1,
)
@TypeConverters(DictionaryTypeConverters::class)
internal abstract class LexploreDatabase : RoomDatabase() {

    abstract val dictionary: DictionaryDao

    companion object {

        fun create(
            context: Context,
        ): LexploreDatabase = Room
            .databaseBuilder(
                context = context,
                klass = LexploreDatabase::class.java,
                name = "lexplore_3",
            )
            .build()
    }
}