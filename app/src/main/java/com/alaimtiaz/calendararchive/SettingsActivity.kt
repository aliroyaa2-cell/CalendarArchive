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
        // Toggle 1: Hide source line
        addToggle(
            container = displayContainer,
            title = "إخفاء المصدر تحت الحدث",
            description = "يخفي السطر اللي فيه Google · الحساب · البريد تحت كل حدث.\n" +
                    "العنوان والوقت يبقون ظاهرين.",
            isChecked = FeatureFlags.isHideSourceEnabled(this),
            onChange = { enabled ->
                FeatureFlags.setHideSourceEnabled(this, enabled)
            }
        )
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

    /**
     * Add a single toggle row to the given container.
     * Uses plain android.widget.Switch for theme stability.
     */
    private fun addToggle(
        container: LinearLayout,
        title: String,
        description: String,
        isChecked: Boolean,
        onChange: (Boolean) -> Unit
    ) {
        // Container row
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setBackgroundColor(0xFF1F1F1F.toInt())
        }

        // Texts container (left side, takes remaining space)
        val textsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }

        val descView = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(0xFFBDBDBD.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        textsContainer.addView(titleView)
        textsContainer.addView(descView)

        // Plain Switch (right side)
        val switchView = Switch(this).apply {
            this.isChecked = isChecked
            setOnCheckedChangeListener { _, checked ->
                onChange(checked)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(12)
            }
        }

        row.addView(textsContainer)
        row.addView(switchView)

        container.addView(row)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
