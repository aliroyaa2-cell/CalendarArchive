package com.alaimtiaz.calendararchive.ui

import android.content.ContentUris
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alaimtiaz.calendararchive.data.EventEntity
import com.alaimtiaz.calendararchive.databinding.ItemEventBinding
import com.alaimtiaz.calendararchive.util.DateUtils

class EventsAdapter : ListAdapter<EventEntity, EventsAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EventEntity>() {
            override fun areItemsTheSame(o: EventEntity, n: EventEntity) = o.instanceKey == n.instanceKey
            override fun areContentsTheSame(o: EventEntity, n: EventEntity) = o == n
        }
    }

    inner class VH(val b: ItemEventBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ev = getItem(position)
        with(holder.b) {
            tvTitle.text = ev.title
            tvTime.text = DateUtils.formatFull(ev.beginMillis, ev.allDay)
            tvSource.text = DateUtils.formatSourceLabel(
                ev.accountType,
                ev.accountName,
                ev.calendarDisplayName
            )

            // Apply calendar color to stripe
            colorStripe.background?.setColorFilter(ev.calendarColor, PorterDuff.Mode.SRC_IN)
            colorStripe.setBackgroundColor(ev.calendarColor)

            itemRoot.setOnClickListener {
                openInCalendar(ev)
            }
        }
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
