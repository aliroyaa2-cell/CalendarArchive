package com.alaimtiaz.calendararchive

import android.content.Context
import android.net.Uri
import android.util.Log
import com.alaimtiaz.calendararchive.data.EventEntity
import com.alaimtiaz.calendararchive.repository.EventsRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles exporting all archived events to a JSON file.
 *
 * Output structure:
 * {
 *   "exportedAt": "2026-05-15T01:23:45Z",
 *   "exportedAtReadable": "2026-05-15 01:23 ص",
 *   "appVersion": "CalendarArchive v1.x",
 *   "totalEvents": 1234,
 *   "events": [
 *     { "title": ..., "beginMillis": ..., ... },
 *     ...
 *   ]
 * }
 */
object ExportManager {

    private const val TAG = "ExportManager"

    /**
     * Export all events to the given URI (chosen by user via SAF).
     *
     * @return number of events exported, or -1 on failure
     */
    suspend fun exportToUri(context: Context, uri: Uri): Int {
        return try {
            val repo = EventsRepository(context)
            val events = repo.getAllEventsForExport()

            val json = buildJson(events)
            val pretty = json.toString(2) // 2-space indent for readability

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(pretty)
                    writer.flush()
                }
            } ?: run {
                Log.e(TAG, "Could not open output stream for URI: $uri")
                return -1
            }

            Log.d(TAG, "Exported ${events.size} events to $uri")
            events.size
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            -1
        }
    }

    /**
     * Build the export JSON object containing metadata + all events.
     */
    private fun buildJson(events: List<EventEntity>): JSONObject {
        val root = JSONObject()

        val now = System.currentTimeMillis()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val readableFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))

        root.put("exportedAt", isoFormat.format(Date(now)))
        root.put("exportedAtReadable", readableFormat.format(Date(now)))
        root.put("appVersion", "CalendarArchive v1.x")
        root.put("totalEvents", events.size)

        val eventsArray = JSONArray()
        for (ev in events) {
            eventsArray.put(eventToJson(ev))
        }
        root.put("events", eventsArray)

        return root
    }

    /**
     * Convert one EventEntity to JSON.
     * Includes all 13 fields from EventEntity.
     */
    private fun eventToJson(ev: EventEntity): JSONObject {
        return JSONObject().apply {
            put("instanceKey", ev.instanceKey)
            put("eventId", ev.eventId)
            put("calendarId", ev.calendarId)
            put("title", ev.title)
            put("description", ev.description ?: JSONObject.NULL)
            put("location", ev.location ?: JSONObject.NULL)
            put("beginMillis", ev.beginMillis)
            put("endMillis", ev.endMillis)
            put("allDay", ev.allDay)
            put("accountName", ev.accountName ?: JSONObject.NULL)
            put("accountType", ev.accountType ?: JSONObject.NULL)
            put("calendarDisplayName", ev.calendarDisplayName ?: JSONObject.NULL)
            put("calendarColor", ev.calendarColor)

            // Bonus: human-readable times for easier reading
            val readableFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            put("beginReadable", readableFormat.format(Date(ev.beginMillis)))
            put("endReadable", readableFormat.format(Date(ev.endMillis)))
        }
    }

    /**
     * Suggest a filename based on current date.
     * Example: calendararchive_export_2026-05-15.json
     */
    fun suggestedFilename(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "calendararchive_export_$date.json"
    }
}
