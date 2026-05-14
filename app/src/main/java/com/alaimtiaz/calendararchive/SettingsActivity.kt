package com.alaimtiaz.calendararchive

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Settings Screen
 *
 * Displays toggles and actions for app configuration.
 * All flags default to OFF — app behaves like stable v1.0.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var displayContainer: LinearLayout
    private lateinit var dataContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        displayContainer = findViewById(R.id.displaySettingsContainer)
        dataContainer = findViewById(R.id.dataSettingsContainer)

        renderDisplaySettings()
        renderDataSettings()
    }

    private fun renderDisplaySettings() {
        // Placeholder — toggles will be added in later phases (B, C)
        addPlaceholder(displayContainer, getString(R.string.settings_empty_display))
    }

    private fun renderDataSettings() {
        // Placeholder — export button will be added in phase D
        addPlaceholder(dataContainer, getString(R.string.settings_empty_data))
    }

    private fun addPlaceholder(container: LinearLayout, text: String) {
        val tv = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(0xFF757575.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(24), dp(12), dp(24))
        }
        container.addView(tv)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
