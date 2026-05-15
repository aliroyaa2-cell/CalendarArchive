package com.alaimtiaz.calendararchive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE beginMillis >= :now ORDER BY beginMillis ASC")
    fun observeUpcoming(now: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE beginMillis < :now ORDER BY beginMillis DESC")
    fun observePast(now: Long): Flow<List<EventEntity>>

    // Search in title AND description
    @Query("""
        SELECT * FROM events
        WHERE beginMillis >= :now
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY beginMillis ASC
    """)
    fun observeUpcomingSearch(now: Long, query: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE beginMillis < :now
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY beginMillis DESC
    """)
    fun observePastSearch(now: Long, query: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("DELETE FROM events WHERE calendarId = :calendarId")
    suspend fun deleteByCalendar(calendarId: Long)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Query("SELECT MAX(beginMillis) FROM events")
    suspend fun getLatestEventTime(): Long?

    @Query("SELECT MIN(beginMillis) FROM events")
    suspend fun getEarliestEventTime(): Long?

    /** Get all events at once — used for export */
    @Query("SELECT * FROM events ORDER BY beginMillis DESC")
    suspend fun getAllForExport(): List<EventEntity>
}
