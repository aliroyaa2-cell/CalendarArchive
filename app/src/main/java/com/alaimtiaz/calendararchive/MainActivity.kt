package com.alaimtiaz.calendararchive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaimtiaz.calendararchive.databinding.ActivityMainBinding
import com.alaimtiaz.calendararchive.ui.EventsAdapter
import com.alaimtiaz.calendararchive.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = EventsAdapter()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            updatePermissionUi()
            viewModel.refreshAndSync()
        } else {
            Toast.makeText(this, "تم رفض الصلاحية", Toast.LENGTH_SHORT).show()
            updatePermissionUi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupTabs()
        setupSearch()
        setupMenu()
        setupPermissionBanner()
        observeViewModel()

        updatePermissionUi()

        if (hasPermission()) {
            viewModel.refreshAndSync()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUi()
    }

    private fun setupRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setTab(
                    if (tab?.position == 0) MainViewModel.Tab.UPCOMING else MainViewModel.Tab.PAST
                )
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.setQuery(query)
                binding.btnClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun setupMenu() {
        binding.btnMenu.setOnClickListener { v ->
            PopupMenu(this, v).apply {
                menuInflater.inflate(R.menu.menu_main, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_source_calendars -> {
                            startActivity(Intent(this@MainActivity, SourceCalendarsActivity::class.java))
                            true
                        }
                        R.id.action_sync_now -> {
                            if (hasPermission()) {
                                viewModel.refreshAndSync()
                            } else {
                                requestPermission()
                            }
                            true
                        }
                        R.id.action_settings -> {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }
    }

    private fun setupPermissionBanner() {
        binding.btnGrantPermission.setOnClickListener {
            requestPermission()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collectLatest { events ->
                        adapter.submitList(events)
                        updateEmptyState(events.isEmpty())
                    }
                }
                launch {
                    viewModel.syncing.collectLatest { syncing ->
                        binding.syncProgress.visibility = if (syncing) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.syncMessage.collectLatest { message ->
                        if (message != null) {
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                            viewModel.consumeSyncMessage()
                        }
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty && hasPermission()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (!hasPermission()) {
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    private fun updatePermissionUi() {
        binding.bannerPermissions.visibility = if (hasPermission()) View.GONE else View.VISIBLE
    }
}
