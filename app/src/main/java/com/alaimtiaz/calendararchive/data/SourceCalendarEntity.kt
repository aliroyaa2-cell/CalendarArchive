package com.alaimtiaz.calendararchive.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source_calendars")
data class SourceCalendarEntity(
    @PrimaryKey
    val calendarId: Long,
    val displayName: String,
    val accountName: String?,
    val accountType: String?,
    val color: Int,
    val enabled: Boolean = true
)
