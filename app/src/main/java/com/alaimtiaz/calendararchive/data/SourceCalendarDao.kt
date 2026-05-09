package com.alaimtiaz.calendararchive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceCalendarDao {

    @Query("SELECT * FROM source_calendars ORDER BY accountType, accountName, displayName")
    fun observeAll(): Flow<List<SourceCalendarEntity>>

    @Query("SELECT * FROM source_calendars ORDER BY accountType, accountName, displayName")
    suspend fun getAll(): List<SourceCalendarEntity>

    @Query("SELECT * FROM source_calendars WHERE enabled = 1")
    suspend fun getEnabled(): List<SourceCalendarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(calendars: List<SourceCalendarEntity>)

    @Query("UPDATE source_calendars SET enabled = :enabled WHERE calendarId = :calendarId")
    suspend fun setEnabled(calendarId: Long, enabled: Boolean)

    @Query("DELETE FROM source_calendars")
    suspend fun deleteAll()
}
