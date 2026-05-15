package com.alaimtiaz.calendararchive.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alaimtiaz.calendararchive.data.EventEntity
import com.alaimtiaz.calendararchive.repository.EventsRepository
import com.alaimtiaz.calendararchive.repository.SourceCalendarsRepository
import com.alaimtiaz.calendararchive.ui.EventsAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val eventsRepo = EventsRepository(application)
    private val sourceRepo = SourceCalendarsRepository(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _tab = MutableStateFlow(Tab.UPCOMING)
    val tab: StateFlow<Tab> = _tab

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    /** Classic tabbed flow — used when unified list flag is OFF */
    @OptIn(ExperimentalCoroutinesApi::class)
    val events: Flow<List<EventEntity>> = combine(_tab, _query) { t, q -> Pair(t, q) }
        .flatMapLatest { (t, q) ->
            when (t) {
                Tab.UPCOMING -> eventsRepo.observeUpcoming(q)
                Tab.PAST -> eventsRepo.observePast(q)
            }
        }

    /** Unified flow — combines upcoming + past with section headers */
    @OptIn(ExperimentalCoroutinesApi::class)
    val unifiedEvents: Flow<List<EventsAdapter.ListItem>> = _query
        .flatMapLatest { q ->
            combine(
                eventsRepo.observeUpcoming(q),
                eventsRepo.observePast(q)
            ) { upcoming, past ->
                buildUnifiedList(upcoming, past)
            }
        }

    private fun buildUnifiedList(
        upcoming: List<EventEntity>,
        past: List<EventEntity>
    ): List<EventsAdapter.ListItem> {
        val result = mutableListOf<EventsAdapter.ListItem>()

        if (upcoming.isNotEmpty()) {
            result.add(
                EventsAdapter.ListItem.HeaderItem(
                    id = "header_upcoming",
                    title = "📅  الأحداث القادمة",
                    count = upcoming.size,
                    isUpcoming = true
                )
            )
            result.addAll(upcoming.map { EventsAdapter.ListItem.EventItem(it) })
        }

        if (past.isNotEmpty()) {
            result.add(
                EventsAdapter.ListItem.HeaderItem(
                    id = "header_past",
                    title = "🕐  الأحداث السابقة",
                    count = past.size,
                    isUpcoming = false
                )
            )
            result.addAll(past.map { EventsAdapter.ListItem.EventItem(it) })
        }

        return result
    }

    fun setTab(tab: Tab) { _tab.value = tab }
    fun setQuery(q: String) { _query.value = q }

    fun consumeSyncMessage() { _syncMessage.value = null }

    /** Refresh source list + sync events */
    fun refreshAndSync() {
        if (_syncing.value) return
        viewModelScope.launch {
            _syncing.value = true
            try {
                sourceRepo.refreshSourceCalendars()
                val count = sourceRepo.syncEvents()
                _syncMessage.value = "اكتملت المزامنة ($count حدث)"
            } catch (e: Exception) {
                _syncMessage.value = "تعذرت المزامنة: ${e.message ?: "خطأ غير معروف"}"
            } finally {
                _syncing.value = false
            }
        }
    }

    enum class Tab { UPCOMING, PAST }
}
