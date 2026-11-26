package com.daymark.app.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daymark.app.domain.models.AppTheme

@Entity(tableName = "custom_themes")
data class CustomThemeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val accentColor: Long,
    val isDark: Boolean = false
) {
    fun toDomain(): AppTheme = AppTheme(
        id = id,
        name = name,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        accentColor = accentColor,
        isDark = isDark,
        isCustom = true
    )

    companion object {
        fun fromDomain(theme: AppTheme): CustomThemeEntity = CustomThemeEntity(
            id = theme.id,
            name = theme.name,
            primaryColor = theme.primaryColor,
            secondaryColor = theme.secondaryColor,
            accentColor = theme.accentColor,
            isDark = theme.isDark
        )
    }
}
