package com.alaimtiaz.calendararchive.ui

import android.content.ContentUris
import android.content.Intent
import android.graphics.PorterDuff
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alaimtiaz.calendararchive.FeatureFlags
import com.alaimtiaz.calendararchive.R
import com.alaimtiaz.calendararchive.data.EventEntity
import com.alaimtiaz.calendararchive.databinding.ItemEventBinding
import com.alaimtiaz.calendararchive.databinding.ItemSectionHeaderBinding
import com.alaimtiaz.calendararchive.util.DateUtils

/**
 * Adapter supports two view types:
 *   - HeaderItem: section header ("Upcoming" / "Past") shown only in unified list mode
 *   - EventItem: a calendar event row
 *
 * Backward compatible: submitting a List<EventEntity> still works via submitEvents().
 */
class EventsAdapter : ListAdapter<EventsAdapter.ListItem, RecyclerView.ViewHolder>(DIFF) {

    /** Sealed type — adapter holds either headers or events */
    sealed class ListItem {
        data class HeaderItem(
            val id: String,
            val title: String,
            val count: Int,
            val isUpcoming: Boolean
        ) : ListItem()

        data class EventItem(val event: EventEntity) : ListItem()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EVENT = 1

        private val DIFF = object : DiffUtil.ItemCallback<ListItem>() {
            override fun areItemsTheSame(o: ListItem, n: ListItem): Boolean {
                return when {
                    o is ListItem.HeaderItem && n is ListItem.HeaderItem -> o.id == n.id
                    o is ListItem.EventItem && n is ListItem.EventItem -> o.event.instanceKey == n.event.instanceKey
                    else -> false
                }
            }
            override fun areContentsTheSame(o: ListItem, n: ListItem) = o == n
        }
    }

    inner class EventVH(val b: ItemEventBinding) : RecyclerView.ViewHolder(b.root)
    inner class HeaderVH(val b: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.HeaderItem -> TYPE_HEADER
            is ListItem.EventItem -> TYPE_EVENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(ItemSectionHeaderBinding.inflate(inflater, parent, false))
            TYPE_EVENT -> EventVH(ItemEventBinding.inflate(inflater, parent, false))
            else -> throw IllegalStateException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.HeaderItem -> bindHeader(holder as HeaderVH, item)
            is ListItem.EventItem -> bindEvent(holder as EventVH, item.event)
        }
    }

    private fun bindHeader(holder: HeaderVH, item: ListItem.HeaderItem) {
        with(holder.b) {
            tvHeaderTitle.text = item.title
            tvHeaderCount.text = item.count.toString()

            // Gray gradient for past section
            if (!item.isUpcoming) {
                headerRoot.setBackgroundResource(R.drawable.section_header_bg_past)
            } else {
                headerRoot.setBackgroundResource(R.drawable.section_header_bg)
            }
        }
    }

    private fun bindEvent(holder: EventVH, ev: EventEntity) {
        val context = holder.b.root.context
        with(holder.b) {
            tvTitle.text = ev.title
            tvTime.text = DateUtils.formatFull(ev.beginMillis, ev.allDay)

            // Hide source line if FeatureFlag is enabled
            if (FeatureFlags.isHideSourceEnabled(context)) {
                tvSource.visibility = View.GONE
            } else {
                tvSource.visibility = View.VISIBLE
                tvSource.text = DateUtils.formatSourceLabel(
                    ev.accountType,
                    ev.accountName,
                    ev.calendarDisplayName
                )
            }

            // Apply calendar color to stripe
            colorStripe.background?.setColorFilter(ev.calendarColor, PorterDuff.Mode.SRC_IN)
            colorStripe.setBackgroundColor(ev.calendarColor)

            itemRoot.setOnClickListener {
                openInCalendar(ev)
            }
        }
    }

    /**
     * Backward-compatible API: submit a flat list of events (no headers).
     * Used in tab mode (when unified list flag is OFF).
     */
    fun submitEvents(events: List<EventEntity>) {
        submitList(events.map { ListItem.EventItem(it) })
    }

    private fun openInCalendar(ev: EventEntity) {
        val context = (recyclerView?.context) ?: return
        try {
            val uri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                ev.eventId
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, ev.beginMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, ev.endMillis)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: just open the calendar at this time
            try {
                val builder = CalendarContract.CONTENT_URI.buildUpon()
                    .appendPath("time")
                ContentUris.appendId(builder, ev.beginMillis)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = builder.build()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "تعذر فتح التقويم", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        super.onAttachedToRecyclerView(rv)
        recyclerView = rv
    }

    override fun onDetachedFromRecyclerView(rv: RecyclerView) {
        super.onDetachedFromRecyclerView(rv)
        recyclerView = null
    }
}
