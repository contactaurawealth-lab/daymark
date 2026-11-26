package com.daymark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daymark.app.core.database.entities.CustomThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomThemeDao {
    @Query("SELECT * FROM custom_themes")
    fun getAllThemes(): Flow<List<CustomThemeEntity>>

    @Query("SELECT * FROM custom_themes")
    suspend fun getAllThemesSync(): List<CustomThemeEntity>

    @Query("SELECT * FROM custom_themes WHERE id = :id")
    suspend fun getThemeById(id: String): CustomThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: CustomThemeEntity)

    @Query("DELETE FROM custom_themes WHERE id = :id")
    suspend fun deleteTheme(id: String)

    @Query("DELETE FROM custom_themes")
    suspend fun clearAll()
}
