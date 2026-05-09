package com.alaimtiaz.calendararchive

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaimtiaz.calendararchive.data.AppDatabase
import com.alaimtiaz.calendararchive.databinding.ActivitySourceCalendarsBinding
import com.alaimtiaz.calendararchive.repository.SourceCalendarsRepository
import com.alaimtiaz.calendararchive.ui.SourceCalendarsAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SourceCalendarsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySourceCalendarsBinding
    private lateinit var sourceRepo: SourceCalendarsRepository
    private lateinit var db: AppDatabase

    private val adapter = SourceCalendarsAdapter { cal, isChecked ->
        // Toggle handled inline; we update DB on save
        lifecycleScope.launch {
            db.sourceCalendarDao().setEnabled(cal.calendarId, isChecked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySourceCalendarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sourceRepo = SourceCalendarsRepository(this)
        db = AppDatabase.getInstance(this)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            // Re-sync with new source selection
            lifecycleScope.launch {
                Toast.makeText(this@SourceCalendarsActivity, "بدأت المزامنة...", Toast.LENGTH_SHORT).show()
                try {
                    val count = sourceRepo.syncEvents()
                    Toast.makeText(this@SourceCalendarsActivity, "اكتملت المزامنة ($count حدث)", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@SourceCalendarsActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Refresh source list on entering this screen
        lifecycleScope.launch {
            sourceRepo.refreshSourceCalendars()
        }

        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.sourceCalendarDao().observeAll().collectLatest { list ->
                    adapter.submitList(list)
                    updateEmptyState(list.isEmpty())
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
        }
    }
}
