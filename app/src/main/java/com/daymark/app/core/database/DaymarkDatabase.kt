package com.daymark.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.daymark.app.core.database.dao.CustomThemeDao
import com.daymark.app.core.database.dao.GoalCheckInDao
import com.daymark.app.core.database.dao.GoalDao
import com.daymark.app.core.database.dao.TaskDao
import com.daymark.app.core.database.dao.TaskListDao
import com.daymark.app.core.database.dao.TimerDao
import com.daymark.app.core.database.entities.CustomThemeEntity
import com.daymark.app.core.database.entities.GoalCheckInEntity
import com.daymark.app.core.database.entities.GoalEntity
import com.daymark.app.core.database.entities.TaskEntity
import com.daymark.app.core.database.entities.TaskListEntity
import com.daymark.app.core.database.entities.TimerSessionEntity

@Database(
    entities = [
        TaskEntity::class,
        TaskListEntity::class,
        GoalEntity::class,
        GoalCheckInEntity::class,
        TimerSessionEntity::class,
        CustomThemeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DaymarkDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun goalDao(): GoalDao
    abstract fun goalCheckInDao(): GoalCheckInDao
    abstract fun timerDao(): TimerDao
    abstract fun customThemeDao(): CustomThemeDao

    companion object {
        @Volatile
        private var INSTANCE: DaymarkDatabase? = null

        fun getDatabase(context: Context): DaymarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DaymarkDatabase::class.java,
                    "daymark_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
