package com.alaimtiaz.calendararchive.repository

import android.content.Context
import com.alaimtiaz.calendararchive.data.AppDatabase
import com.alaimtiaz.calendararchive.data.EventEntity
import kotlinx.coroutines.flow.Flow

class EventsRepository(context: Context) {

    private val db: AppDatabase = AppDatabase.getInstance(context)

    fun observeUpcoming(query: String = ""): Flow<List<EventEntity>> {
        val now = System.currentTimeMillis()
        return if (query.isBlank()) {
            db.eventDao().observeUpcoming(now)
        } else {
            db.eventDao().observeUpcomingSearch(now, query.trim())
        }
    }

    fun observePast(query: String = ""): Flow<List<EventEntity>> {
        val now = System.currentTimeMillis()
        return if (query.isBlank()) {
            db.eventDao().observePast(now)
        } else {
            db.eventDao().observePastSearch(now, query.trim())
        }
    }

    suspend fun count(): Int = db.eventDao().count()
}
