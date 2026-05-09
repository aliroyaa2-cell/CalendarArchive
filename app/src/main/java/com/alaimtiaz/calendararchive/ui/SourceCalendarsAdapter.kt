package com.alaimtiaz.calendararchive.ui

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alaimtiaz.calendararchive.data.SourceCalendarEntity
import com.alaimtiaz.calendararchive.databinding.ItemSourceCalendarBinding
import com.alaimtiaz.calendararchive.util.DateUtils

class SourceCalendarsAdapter(
    private val onToggle: (SourceCalendarEntity, Boolean) -> Unit
) : ListAdapter<SourceCalendarEntity, SourceCalendarsAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SourceCalendarEntity>() {
            override fun areItemsTheSame(o: SourceCalendarEntity, n: SourceCalendarEntity) = o.calendarId == n.calendarId
            override fun areContentsTheSame(o: SourceCalendarEntity, n: SourceCalendarEntity) = o == n
        }
    }

    inner class VH(val b: ItemSourceCalendarBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSourceCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cal = getItem(position)
        with(holder.b) {
            tvName.text = cal.displayName
            tvAccount.text = DateUtils.formatSourceLabel(cal.accountType, cal.accountName, null)

            colorDot.setBackgroundColor(cal.color)

            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = cal.enabled
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(cal, isChecked)
            }

            itemRoot.setOnClickListener {
                switchEnabled.isChecked = !switchEnabled.isChecked
            }
        }
    }
}
