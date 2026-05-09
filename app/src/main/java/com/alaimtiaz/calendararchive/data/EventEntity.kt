package com.alaimtiaz.calendararchive.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["beginMillis"]),
        Index(value = ["title"]),
        Index(value = ["calendarId"])
    ]
)
data class EventEntity(
    @PrimaryKey
    val instanceKey: String,           // eventId + "_" + beginMillis
    val eventId: Long,                 // Original event id from CalendarContract
    val calendarId: Long,
    val title: String,
    val description: String?,
    val location: String?,
    val beginMillis: Long,             // Start time
    val endMillis: Long,               // End time
    val allDay: Boolean,
    val accountName: String?,
    val accountType: String?,
    val calendarDisplayName: String?,
    val calendarColor: Int
)
