package com.alaimtiaz.calendararchive.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.alaimtiaz.calendararchive.data.AppDatabase
import com.alaimtiaz.calendararchive.data.EventEntity
import com.alaimtiaz.calendararchive.data.SourceCalendarEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class SourceCalendarsRepository(private val context: Context) {

    private val db: AppDatabase = AppDatabase.getInstance(context)

    companion object {
        // FIXED start: January 1, 2000 — never changes
        private const val ARCHIVE_START_YEAR = 2000

        // Sliding end: today + 5 years
        private const val FUTURE_YEARS = 5
    }

    /** Returns the fixed window for syncing (2000-01-01 → today + 5 years) */
    private fun getSyncWindow(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            clear()
            set(ARCHIVE_START_YEAR, Calendar.JANUARY, 1, 0, 0, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            add(Calendar.YEAR, FUTURE_YEARS)
        }.timeInMillis

        return Pair(start, end)
    }

    /** Reads all calendars from device and saves to DB (preserves enabled state) */
    suspend fun refreshSourceCalendars(): Int = withContext(Dispatchers.IO) {
        val calendars = mutableListOf<SourceCalendarEntity>()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR
        )

        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val typeIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
            val colorIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)

            while (it.moveToNext()) {
                calendars.add(
                    SourceCalendarEntity(
                        calendarId = it.getLong(idIdx),
                        displayName = it.getString(nameIdx) ?: "Unknown",
                        accountName = it.getString(accountIdx),
                        accountType = it.getString(typeIdx),
                        color = it.getInt(colorIdx),
                        enabled = true   // default new ones to enabled
                    )
                )
            }
        }

        // Preserve existing enabled state
        val existing = db.sourceCalendarDao().getAll().associateBy { it.calendarId }
        val merged = calendars.map { newCal ->
            existing[newCal.calendarId]?.let { existingCal ->
                newCal.copy(enabled = existingCal.enabled)
            } ?: newCal
        }

        db.sourceCalendarDao().upsertAll(merged)
        return@withContext merged.size
    }

    /** Syncs events from enabled calendars within the fixed window */
    suspend fun syncEvents(): Int = withContext(Dispatchers.IO) {
        val enabledCalendars = db.sourceCalendarDao().getEnabled()
        if (enabledCalendars.isEmpty()) {
            db.eventDao().deleteAll()
            return@withContext 0
        }

        val (windowStart, windowEnd) = getSyncWindow()
        val allEvents = mutableListOf<EventEntity>()

        // Build calendar info map for quick lookup
        val calMap = enabledCalendars.associateBy { it.calendarId }
        val enabledIds = enabledCalendars.map { it.calendarId }.toSet()

        // Use Instances API to handle recurring events
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, windowStart)
        ContentUris.appendId(builder, windowEnd)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )

        val cursor: Cursor? = context.contentResolver.query(
            builder.build(),
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )

        cursor?.use {
            val eventIdIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val calIdIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
            val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val descIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val locIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val beginIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)

            while (it.moveToNext()) {
                val calId = it.getLong(calIdIdx)
                if (calId !in enabledIds) continue

                val eventId = it.getLong(eventIdIdx)
                val begin = it.getLong(beginIdx)
                val cal = calMap[calId]

                allEvents.add(
                    EventEntity(
                        instanceKey = "${eventId}_${begin}",
                        eventId = eventId,
                        calendarId = calId,
                        title = it.getString(titleIdx) ?: "(بدون عنوان)",
                        description = it.getString(descIdx),
                        location = it.getString(locIdx),
                        beginMillis = begin,
                        endMillis = it.getLong(endIdx),
                        allDay = it.getInt(allDayIdx) == 1,
                        accountName = cal?.accountName,
                        accountType = cal?.accountType,
                        calendarDisplayName = cal?.displayName,
                        calendarColor = cal?.color ?: 0xFF1976D2.toInt()
                    )
                )
            }
        }

        // Replace all events
        db.eventDao().deleteAll()
        // Insert in chunks to avoid SQLite limits
        allEvents.chunked(500).forEach { chunk ->
            db.eventDao().upsertAll(chunk)
        }

        return@withContext allEvents.size
    }
}
